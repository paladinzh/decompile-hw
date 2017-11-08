package com.huawei.gallery.refocus.wideaperture.app;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.app.GLHost;
import com.huawei.gallery.refocus.app.AbsRefocusController;
import com.huawei.gallery.refocus.app.AbsRefocusDelegate;
import com.huawei.gallery.refocus.app.RefocusPage.ActionInfo;
import com.huawei.gallery.refocus.app.RefocusPage.WideAperturePhotoPhotoEditorDelegateAbs;
import com.huawei.gallery.refocus.app.State;
import com.huawei.gallery.refocus.ui.RefocusIndicator;
import com.huawei.gallery.refocus.ui.RefocusIndicator.onWideApertureListener;
import com.huawei.gallery.refocus.wideaperture.ui.ApertureMenu.MENU;
import com.huawei.gallery.refocus.wideaperture.ui.CategoryAdapter;
import com.huawei.gallery.refocus.wideaperture.ui.CategoryAdapter.OnSelectedChangedListener;
import com.huawei.gallery.refocus.wideaperture.ui.CategoryTrack;
import com.huawei.gallery.refocus.wideaperture.ui.RefocusActionBar;
import com.huawei.gallery.refocus.wideaperture.ui.RefocusActionBar.UIListener;

public class WideApertureState extends State implements onWideApertureListener, OnClickListener, UIListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private RefocusActionBar mActionBar;
    private CategoryAdapter mAdapter;
    private int mCurrentFilterIndex;
    private FrameLayout mFilterScrollView;
    private ViewGroup mFootGroupRoot;
    private OnSelectedChangedListener mOnSelectedChangedListener;
    private CategoryTrack mPanel;
    private AlertDialog mSaveTipsDialog;
    private final TransitionStore mTransitionStore = new TransitionStore();
    private ApertureParameter mWideAperturePara;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 8;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 10;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 11;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 12;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 14;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 15;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 16;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 17;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 18;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 19;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 20;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 21;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 22;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 23;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 24;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 25;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 26;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 27;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 28;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 29;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 30;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 31;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 32;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 33;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 34;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 35;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 36;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 37;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 2;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 38;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 39;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 3;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 40;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 41;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 42;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 43;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 44;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 45;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 46;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 47;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 48;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 49;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 50;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 51;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 52;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 53;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 54;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 55;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 57;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 58;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 59;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 60;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 61;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 62;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 63;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 64;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 65;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 66;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 67;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 68;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 69;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 70;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 71;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 72;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 73;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 74;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 75;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 76;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 77;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 4;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 78;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 79;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 80;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 81;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 82;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 83;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 84;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 85;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 87;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 88;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 89;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 90;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 91;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 92;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 93;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 94;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 96;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 97;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 98;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 99;
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

    public WideApertureState(GalleryContext context, View parentLayout, AbsRefocusDelegate delegate) {
        super(context, parentLayout, delegate);
    }

    public boolean onActionItemClick(Action action) {
        if (this.mActionBar != null && this.mActionBar.isSeekBarTrackingTouch()) {
            return true;
        }
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return showSaveTips(action);
            case 4:
                if (!this.mEnableSaveAs || this.mDisableRefocusAndExit) {
                    GalleryLog.i("WideApertureState", "disable any operations");
                    return false;
                }
                if (!(this.mEditorController == null || this.mActionBar == null)) {
                    enableOperations(false);
                    enableSaveAction();
                    if (this.mFocusIndicator != null) {
                        this.mFocusIndicator.clear();
                    }
                    this.mActionBar.setVisibility(4);
                    this.mEditorDelegate.sendEmptyMessage(this.mEditorDelegate.getShowProgressMessageID());
                    this.mEditorController.saveAs();
                    ReportToBigData.report(43, this.mEditorController.getSaveMessage());
                    ReportToBigData.reportBigDataForUsedFilter(this.mCurrentFilterIndex);
                }
                return true;
            default:
                return true;
        }
    }

    public boolean onBackPressed() {
        return showSaveTips(Action.BACK);
    }

    public boolean doSaveAndExit() {
        if (!this.mEnableDoRefocus || !this.mEnableSaveAs || this.mDisableRefocusAndExit) {
            return true;
        }
        if (this.mFocusIndicator != null) {
            this.mFocusIndicator.clear();
        }
        this.mActionBar.setVisibility(4);
        this.mDisableRefocusAndExit = true;
        enableOperations(false);
        enableSaveAction();
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getDoRefocusMessageID());
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getApplyFilterMessageID());
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getRefocusSaveMessageID());
        this.mEditorDelegate.sendEmptyMessage(this.mEditorDelegate.getRefocusSaveMessageID());
        return true;
    }

    public void resume() {
    }

    public void pause() {
        ContextedUtils.hideToast(this.mContext.getActivityContext());
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getDoRefocusMessageID());
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getApplyFilterMessageID());
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getRefocusSaveMessageID());
    }

    public void applyFilter(Message msg) {
        if (!this.mEditorController.isRefocusPhoto()) {
            return;
        }
        if (this.mEnableDoRefocus && this.mEnableSaveAs) {
            this.mEnableDoRefocus = false;
            this.mCurrentFilterIndex = msg.arg1;
            ((WideAperturePhotoController) this.mEditorController).applyFilter(((WideApertureFilterAction) this.mAdapter.getItem(this.mCurrentFilterIndex)).getFilterType());
            return;
        }
        this.mEditorDelegate.sendMessageDelayed(this.mEditorDelegate.getApplyFilterMessageID(), msg.arg1, msg.arg2, 30);
    }

    public RefocusIndicator getFocusIndicatorView() {
        return this.mFocusIndicator;
    }

    public ProgressBar getProgressBar() {
        return this.mProgressbar;
    }

    public ActionInfo getActionInfo() {
        return this.mActionInfo;
    }

    public AbsRefocusController getAllFocusController() {
        return this.mEditorController;
    }

    public void onLayoutChanged(GLHost host, int naviHeight) {
        changeActionBarLayout(naviHeight);
    }

    private void changeActionBarLayout(int naviHeight) {
        LayoutParams layoutParams = (LayoutParams) this.mActionBar.getLayoutParams();
        if (isPort()) {
            layoutParams.bottomMargin = naviHeight;
            layoutParams.rightMargin = 0;
        } else {
            layoutParams.bottomMargin = 0;
            layoutParams.rightMargin = naviHeight;
        }
        this.mActionBar.setLayoutParams(layoutParams);
    }

    public void onWideApertureValueChanged(int value) {
        this.mEditorDelegate.removeMessages(this.mEditorDelegate.getWideApertureValueChangedMessageID());
        if (this.mEnableDoRefocus && this.mEnableSaveAs) {
            this.mEnableDoRefocus = false;
            this.mEditorController.setWideApertureValue(value);
            return;
        }
        this.mEditorDelegate.sendMessageDelayed(this.mEditorDelegate.getWideApertureValueChangedMessageID(), value, 0, 30);
    }

    public boolean needSupportWideAperture() {
        return true;
    }

    public int getScrollViewHeight() {
        return GalleryUtils.dpToPixel(115) + ((LayoutParams) this.mActionBar.getLayoutParams()).bottomMargin;
    }

    public int getScrollViewWidth() {
        return GalleryUtils.dpToPixel(100) + ((LayoutParams) this.mActionBar.getLayoutParams()).rightMargin;
    }

    public void resetIndicatorLocation() {
        resetRefocusPoint();
    }

    public void showFootGroupView() {
        if (this.mFootGroupRoot != null && this.mEnableSaveAs) {
            this.mFootGroupRoot.setVisibility(0);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        boolean z = true;
        if (this.mView != null) {
            saveUISelectionSate();
            removeOldUI();
            if (config.orientation != 1) {
                z = false;
            }
            inflateFootGroupView(z);
            restoreUISelection();
            showFootGroupView();
        }
    }

    private void resetRefocusPoint() {
        int i = 0;
        Point currentRefocusPoint = ((WideAperturePhotoController) this.mEditorController).getCurrentDoRefocusPointInImage();
        if (currentRefocusPoint.x >= 0 && currentRefocusPoint.y >= 0) {
            int i2;
            Point imageRightBottomPoint = ((WideAperturePhotoPhotoEditorDelegateAbs) this.mEditorDelegate).getImageRightBottomPoint();
            Point currentImageRightBottomPoint = this.mEditorDelegate.transformToScreenCoordinate(imageRightBottomPoint);
            Point currentImageLeftTopPoint = this.mEditorDelegate.transformToScreenCoordinate(new Point(0, 0));
            GalleryLog.d("WideApertureState", String.format("pointInScreenCoordinate(%d, %d)", new Object[]{Integer.valueOf(resultPoint.x), Integer.valueOf(new Point((int) (((((float) currentRefocusPoint.x) / ((float) imageRightBottomPoint.x)) * ((float) (currentImageRightBottomPoint.x - currentImageLeftTopPoint.x))) + ((float) currentImageLeftTopPoint.x)), (int) (((((float) currentRefocusPoint.y) / ((float) imageRightBottomPoint.y)) * ((float) (currentImageRightBottomPoint.y - currentImageLeftTopPoint.y))) + ((float) currentImageLeftTopPoint.y))).y)}));
            Rect focusEdge = new Rect();
            focusEdge.left = currentImageLeftTopPoint.x > 0 ? currentImageLeftTopPoint.x : 0;
            if (currentImageLeftTopPoint.y > 0) {
                i = currentImageLeftTopPoint.y;
            }
            focusEdge.top = i;
            if (currentImageRightBottomPoint.x < this.mView.getWidth()) {
                i2 = currentImageRightBottomPoint.x;
            } else {
                i2 = this.mView.getWidth();
            }
            focusEdge.right = i2;
            if (currentImageRightBottomPoint.y < this.mView.getHeight()) {
                i2 = currentImageRightBottomPoint.y;
            } else {
                i2 = this.mView.getHeight();
            }
            focusEdge.bottom = i2;
            setRefocusIndicatorLocation(resultPoint, focusEdge);
        }
    }

    private void setRefocusIndicatorLocation(Point resultPoint, Rect focusEdge) {
        if (resultPoint.x >= 0 && resultPoint.y >= 0) {
            this.mFocusIndicator.setScale(focusEdge);
            this.mFocusIndicator.setLocationCoordinate(resultPoint.x, resultPoint.y);
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        changeActionBarLayout(height);
        this.mEditorDelegate.refreshLayout();
    }

    private void saveUISelectionSate() {
        this.mActionBar.saveSelectionState(this.mTransitionStore);
    }

    private void restoreUISelection() {
        this.mActionBar.restoreOperationMenuSelectionState(this.mTransitionStore);
        setFilterSelectionState();
    }

    private void removeOldUI() {
        if (this.mFootGroupRoot != null) {
            this.mFootGroupRoot.removeAllViews();
            this.mFootGroupRoot.setVisibility(8);
            this.mFootGroupRoot = null;
        }
    }

    public void onClick(View v) {
        if (v instanceof SimpleActionItem) {
            onActionItemClick(((SimpleActionItem) v).getAction());
        }
    }

    private boolean showSaveTips(Action action) {
        if (!this.mEditorController.ifPhotoChanged()) {
            this.mActionBar.setVisibility(4);
            this.mDisableRefocusAndExit = false;
            this.mEditorDelegate.finishActivity();
            return false;
        } else if (!this.mEditorController.ifPhotoChanged() || action == Action.OK) {
            doSaveAndExit();
            return true;
        } else if (action == Action.NO) {
            ((WideAperturePhotoController) this.mEditorController).restoreOriginalRefocusPoint();
            doSaveAndExit();
            return true;
        } else {
            if (this.mSaveTipsDialog == null) {
                this.mSaveTipsDialog = createSaveTipsDialog();
            }
            if (!this.mSaveTipsDialog.isShowing()) {
                this.mSaveTipsDialog.show();
            }
            GalleryUtils.setTextColor(this.mSaveTipsDialog.getButton(-2), this.mContext.getActivityContext().getResources());
            return true;
        }
    }

    private AlertDialog createSaveTipsDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    ((WideAperturePhotoController) WideApertureState.this.mEditorController).restoreOriginalRefocusPoint();
                }
                WideApertureState.this.doSaveAndExit();
            }
        };
        return new Builder(this.mContext.getActivityContext()).setMessage(R.string.photoeditor_unsave_Confirm_Msg).setNegativeButton(R.string.gallery_no, listener).setPositiveButton(R.string.gallery_yes, listener).create();
    }

    private void inflateFootGroupView(boolean isPort) {
        int darkThemeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
        int nomramThemeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        this.mFootGroupRoot = (ViewGroup) this.mView.findViewById(isPort ? R.id.editor_foot_bar_port : R.id.editor_foot_bar_land);
        this.mContext.getActivityContext().getTheme().applyStyle(nomramThemeId, true);
        ((LayoutInflater) this.mContext.getActivityContext().getSystemService("layout_inflater")).inflate(isPort ? R.layout.refocus_action_bar : R.layout.refocus_action_bar_land, this.mFootGroupRoot, true);
        this.mFilterScrollView = (FrameLayout) this.mFootGroupRoot.findViewById(R.id.itemScrollView);
        this.mActionBar = (RefocusActionBar) this.mFootGroupRoot.findViewById(R.id.refocus_action_bar);
        this.mActionBar.initialize(this);
        this.mContext.getActivityContext().getTheme().applyStyle(darkThemeId, true);
        SimpleActionItem leftActionItem = (SimpleActionItem) this.mFootGroupRoot.findViewById(R.id.foot_select_left);
        SimpleActionItem rightActionItem = (SimpleActionItem) this.mFootGroupRoot.findViewById(R.id.foot_select_right);
        leftActionItem.setAction(Action.NO);
        rightActionItem.setAction(Action.OK);
        leftActionItem.setOnClickListener(this);
        rightActionItem.setOnClickListener(this);
        this.mAdapter = new CategoryAdapter(this.mContext.getActivityContext());
        this.mAdapter.setOrientation(isPort ? 1 : 0);
        this.mAdapter.initializeSelection();
        this.mPanel = (CategoryTrack) this.mView.findViewById(R.id.listItems);
        this.mPanel.setAdapter(this.mAdapter);
        this.mAdapter.setContainer(this.mPanel);
        this.mOnSelectedChangedListener = new OnSelectedChangedListener() {
            public void onSelectedChanged(int selected) {
                if (selected >= 0 && selected < WideApertureState.this.mAdapter.getCount()) {
                    if (WideApertureState.this.mFocusIndicator != null) {
                        WideApertureState.this.mFocusIndicator.clear();
                    }
                    if (WideApertureState.this.mCurrentFilterIndex != selected) {
                        WideApertureState.this.mEditorDelegate.removeMessages(WideApertureState.this.mEditorDelegate.getApplyFilterMessageID());
                        WideApertureState.this.mEditorDelegate.sendMessageDelayed(WideApertureState.this.mEditorDelegate.getApplyFilterMessageID(), selected, 0, 30);
                    }
                }
            }
        };
        this.mAdapter.setSelectedChangedListener(this.mOnSelectedChangedListener);
        fillAllfocusFilters();
    }

    protected void initView() {
        this.mFocusIndicator = (RefocusIndicator) this.mView.findViewById(R.id.focus_indicator);
        this.mProgressbar = (ProgressBar) this.mView.findViewById(R.id.progress_bar);
        inflateFootGroupView(isPort());
        this.mWideAperturePara.setLevelCount(((WideAperturePhotoController) this.mEditorController).getWideApertureLevel());
        this.mFocusIndicator.setWideApertureParameter(this.mWideAperturePara);
        this.mFocusIndicator.setOnWideApertureListener(this);
    }

    private boolean isPort() {
        return this.mView.getResources().getConfiguration().orientation == 1;
    }

    protected void initData() {
        this.mActionInfo = new ActionInfo(this.mContext.getResources().getString(R.string.Title_Gallery_Allfocus_wide_aperture), Action.BACK, Action.SAVE, false);
        this.mWideAperturePara = new ApertureParameter();
        this.mEditorController = new WideAperturePhotoController(this.mContext.getActivityContext(), this.mEditorDelegate);
        ((WideAperturePhotoController) this.mEditorController).setViewMode(0);
        this.mEditorController.prepare();
    }

    private boolean fillAllfocusFilters() {
        for (int i = 0; i < WideApertureFilter.getFilterCount(); i++) {
            this.mAdapter.add(new WideApertureFilterAction(this.mContext.getActivityContext(), WideApertureFilter.getFilter(i), ((WideAperturePhotoController) this.mEditorController).getWideAperturePhotoMode()));
        }
        if (((WideAperturePhotoController) this.mEditorController).getWideAperturePhotoMode() == 1) {
            this.mPanel.getChildAt(WideApertureFilter.MONO.ordinal()).setVisibility(8);
        }
        return true;
    }

    public void scrollToSelectedFilter() {
        if (this.mEditorController != null) {
            this.mCurrentFilterIndex = ((WideAperturePhotoController) this.mEditorController).getFilterTypeIndex();
            setFilterSelectionState();
        }
    }

    private void setFilterSelectionState() {
        View selectedView = this.mPanel.getChildAt(this.mCurrentFilterIndex);
        this.mAdapter.setSelected(selectedView);
        this.mFilterScrollView.requestChildFocus(this.mPanel.getChildAt(this.mCurrentFilterIndex), selectedView);
    }

    public void showActionToast() {
        ContextedUtils.showToastQuickly(this.mContext.getActivityContext(), (int) R.string.Title_Gallery_visual_effect_refocus_toast, 1);
    }

    public void setWideApertureValue(int value) {
        this.mWideAperturePara.setValue(value);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public void enableSaveAction() {
        if (this.mEditorController.isRefocusPhoto() && this.mEditorController.prepareComplete() && this.mEnableSaveAs) {
            this.mEditorDelegate.enableSaveAction(true);
        } else {
            this.mEditorDelegate.enableSaveAction(false);
        }
        if (this.mActionBar != null) {
            this.mActionBar.prepareComplete();
        }
        scrollToSelectedFilter();
    }

    public void disableExitEditPage(boolean enabled) {
        this.mDisableRefocusAndExit = enabled;
    }

    public void enableOperations(boolean enabled) {
        this.mEnableDoRefocus = enabled;
        this.mEnableSaveAs = enabled;
        setAllViewsClickable(enabled);
    }

    private void setAllViewsClickable(boolean clickable) {
        if (this.mPanel != null) {
            this.mPanel.setAllViewsClickable(clickable);
        }
        this.mActionBar.setAllViewsClickable(clickable);
    }

    public void onMenuChanged(MENU menu) {
    }

    public void onProgressChanged(int progress) {
        this.mWideAperturePara.setValue(progress);
        onWideApertureValueChanged(this.mWideAperturePara.getValue());
    }

    public int getCurrentSeekbarValue() {
        return (int) ((((float) ((WideAperturePhotoController) this.mEditorController).getRefocusFnumValue()) / ((float) (((WideAperturePhotoController) this.mEditorController).getWideApertureLevel() - 1))) * 100.0f);
    }

    public int getLevelCount() {
        return ((WideAperturePhotoController) this.mEditorController).getWideApertureLevel();
    }

    public void showRefocusIndictor(float progress) {
        this.mWideAperturePara.setValue(this.mActionBar.progressTransform(progress, 100.0f));
        this.mFocusIndicator.reshowRefocusIndicator(progress / 100.0f);
    }

    public String getCurrentTextValue() {
        return this.mWideAperturePara.getShowingValue();
    }
}
