package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.IntentChooser;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.app.LongTapManager;
import com.android.gallery3d.app.LongTapManager.OnItemClickedListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BucketHelper;
import com.android.gallery3d.data.CloudLocalAlbum;
import com.android.gallery3d.data.CloudLocalAlbumSet;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.VirtualAlbum;
import com.android.gallery3d.menuexecutor.MenuEnableCtrller;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.ActionDeleteAndConfirm;
import com.android.gallery3d.ui.ActionRecycleAndConfirm;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.MenuExecutor.ProgressListener;
import com.android.gallery3d.ui.MenuExecutor.SimpleProgressListener;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SelectionManager.SelectionListener;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.gallery.actionbar.AbstractTitleMode;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.app.CreateAlbumDialog.CallBackListner;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.PhotoShareItem;
import com.huawei.gallery.photoshare.ui.PhotoShareLoginActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.RefreshHelper;
import com.huawei.gallery.recycle.app.RecycleAlbumActivity;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.DragListView;
import com.huawei.gallery.util.DragListView.DropListener;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.PermissionManager;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.UpdateConfig;

public class ListAlbumSetFragment extends AbstractGalleryFragment implements OnItemClickListener, OnItemLongClickListener, SelectionListener, CallBackListner {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private int[] PASTE_TYPES = new int[]{1, 2};
    private GalleryActionBar mActionBar;
    private CreateAlbumDialog mAlbumDialog;
    protected AlbumSetDataLoader mAlbumSetDataLoader;
    protected DragListView mAlbumSetList;
    protected ListAlbumSetDataAdapter mAlbumSetListAdapter;
    protected Activity mContext;
    private ActionDeleteAndConfirm mDeleteDialog;
    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private final DropListener mDropListener = new DropListener() {
        public void onDragStart(int position) {
            ListAlbumSetFragment.this.mIsDragging = true;
            ListAlbumSetFragment.this.mActionBar.setActionClickable(false);
            ListAlbumSetFragment.this.mAlbumSetDataLoader.onDragStart(position);
            ListAlbumSetFragment.this.mAlbumSetListAdapter.setDraggingStatus(true);
        }

        public void onDragEnd() {
            ListAlbumSetFragment.this.mAlbumSetDataLoader.onDragEnd();
            ListAlbumSetFragment.this.mIsDragging = false;
            ListAlbumSetFragment.this.mActionBar.setActionClickable(true);
            ListAlbumSetFragment.this.mAlbumSetListAdapter.setDraggingStatus(false);
        }

        public void drop(int from, int to) {
            if (from != to) {
                ListAlbumSetFragment.this.mIsDragSortUsed = true;
                MediaSet fromMediaSet = ListAlbumSetFragment.this.mAlbumSetDataLoader.getDraggedSet();
                MediaSet toMediaSet = ListAlbumSetFragment.this.mAlbumSetDataLoader.getMediaSet(to);
                if (ListAlbumSetFragment.this.mAlbumSetDataLoader.exchangeMediaSet(from, to)) {
                    ListAlbumSetFragment.this.exchangeSortIndex(fromMediaSet, toMediaSet);
                }
                ListAlbumSetFragment.this.mAlbumSetListAdapter.notifyDataSetChanged();
                ListAlbumSetFragment.this.mAlbumSetList.invalidateViews();
                ListAlbumSetFragment.this.mHandler.removeCallbacks(ListAlbumSetFragment.this.mUpdateViewRunnable);
                ListAlbumSetFragment.this.mHandler.postDelayed(ListAlbumSetFragment.this.mUpdateViewRunnable, 500);
            }
        }
    };
    private View mEmptyListView;
    protected Handler mHandler = new Handler();
    private IntentChooser mIntentChooser;
    private boolean mIsActive = false;
    private boolean mIsCloud = false;
    private boolean mIsDragSortUsed;
    private boolean mIsDragging;
    private boolean mIsOutside = false;
    private View mLoadingTipView;
    private LongTapManager mLongTapManager;
    private int mLongTapSlotIndex;
    protected MediaSet mMediaSet;
    protected MenuExecutor mMenuExecutor;
    private ArrayList<Path> mPasteList;
    private ProgressListener mPasteProcessListener = new SimpleProgressListener() {
        private boolean shouldTransferToAlbum = false;

        public void onProgressExecuteSuccess(String path) {
            this.shouldTransferToAlbum = true;
        }

        public void onProgressComplete(int result) {
            if (this.shouldTransferToAlbum) {
                this.shouldTransferToAlbum = false;
                Bundle data = new Bundle();
                data.putString("media-path", "/local/all/outside/" + GalleryUtils.getBucketId(ListAlbumSetFragment.this.mTargetPath));
                Intent intent = new Intent(ListAlbumSetFragment.this.mContext, SlotAlbumActivity.class);
                intent.putExtras(data);
                ListAlbumSetFragment.this.startActivity(intent);
            }
        }
    };
    protected PhotoShareItem mPhotoShareItem;
    private ProgressDialog mProgressDialog;
    private ProgressListener mRenameProcessListener = new SimpleProgressListener() {
        public void onProgressComplete(int result) {
            if (result == 2) {
                ContextedUtils.showToastQuickly(ListAlbumSetFragment.this.mContext, ListAlbumSetFragment.this.mContext.getString(R.string.rename_album_fail_Toast), 0);
            }
        }
    };
    private ArrayList<Integer> mSelectedPositions = new ArrayList();
    protected SelectionManager mSelectionManager;
    protected SelectionMode mSelectionMode;
    private final Runnable mShowDeletingDialogRunnable = new Runnable() {
        public void run() {
            ListAlbumSetFragment.this.showProgressDialog(ListAlbumSetFragment.this.mContext.getString(R.string.delete));
        }
    };
    protected boolean mShowDetails;
    private String mTargetAlbumName;
    private MediaSet mTargetMediaSet;
    private String mTargetPath;
    private final Runnable mUpdateViewRunnable = new Runnable() {
        public void run() {
            ListAlbumSetFragment.this.mAlbumSetListAdapter.notifyDataSetChanged();
            ListAlbumSetFragment.this.mAlbumSetList.invalidateViews();
        }
    };

    private class MyDetailsSource implements DetailsSource {
        private int mIndex;

        private MyDetailsSource() {
        }

        public int setIndex() {
            ArrayList<Path> selectedList = ListAlbumSetFragment.this.mSelectionManager.getSelected(false);
            if (selectedList.size() == 0) {
                this.mIndex = ListAlbumSetFragment.this.mLongTapSlotIndex;
            } else {
                this.mIndex = ListAlbumSetFragment.this.mAlbumSetDataLoader.findSet((Path) selectedList.get(0));
            }
            return this.mIndex;
        }

        public MediaDetails getDetails() {
            MediaObject item = ListAlbumSetFragment.this.mAlbumSetDataLoader.getMediaSet(this.mIndex);
            if (item != null) {
                return item.getDetails();
            }
            GalleryLog.printDFXLog("ListAlbumSetFragment for DFX MediaObject is null");
            return null;
        }
    }

    private class MyLoadingListener implements LoadingListener {
        private MyLoadingListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            GalleryLog.d("PF", "list album set fragment data load finished");
            ListAlbumSetFragment.this.mActionBar.disableAnimation(false);
            ListAlbumSetFragment.this.mLoadingTipView.setVisibility(8);
            if (ListAlbumSetFragment.this.mIsActive && ListAlbumSetFragment.this.mAlbumSetDataLoader.size() == 0) {
                ListAlbumSetFragment.this.mAlbumSetList.setEmptyView(ListAlbumSetFragment.this.mEmptyListView);
                ListAlbumSetFragment.updateEmptyLayoutPadding(ListAlbumSetFragment.this.getActivity(), ListAlbumSetFragment.this.mEmptyListView);
            }
            if (!ListAlbumSetFragment.this.mIsActive || !ListAlbumSetFragment.this.mSelectionManager.inSelectionMode()) {
                return;
            }
            if (ListAlbumSetFragment.this.mSelectionManager.getSelectedCount() != ListAlbumSetFragment.this.mSelectionManager.getTotalCount()) {
                ListAlbumSetFragment.this.mSelectionMode.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
                return;
            }
            ListAlbumSetFragment.this.mSelectionManager.selectAll();
            ListAlbumSetFragment.this.mSelectionMode.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
        }

        public void onVisibleRangeLoadFinished() {
            ListAlbumSetFragment.this.mLoadingTipView.setVisibility(8);
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 16;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 17;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 18;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 19;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 20;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 21;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 22;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 23;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 24;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 3;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 4;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 5;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 25;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 26;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 27;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 28;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 6;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 29;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 30;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 31;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 32;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 33;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 34;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 35;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 7;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 8;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 9;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 36;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 37;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 38;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 39;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 40;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 10;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 41;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 42;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 11;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 43;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 44;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 12;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 45;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 46;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 47;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 48;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 49;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 50;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 51;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 52;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 53;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 54;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 55;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 56;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 57;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 58;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 60;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 61;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 62;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 63;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 64;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 65;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 66;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 67;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 68;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 69;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 70;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 71;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 72;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 73;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 74;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 75;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 13;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 76;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 77;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 78;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 79;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 80;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 81;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 82;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 83;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 84;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 85;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 86;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 87;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 88;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 89;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 14;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 15;
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

    public void onCreate(Bundle savedInstanceState) {
        boolean z = true;
        TraceController.beginSection("ListAlbumSetFragment.onCreate");
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data == null) {
            TraceController.endSection();
            return;
        }
        if (savedInstanceState != null) {
            this.mTargetPath = savedInstanceState.getString("key-targetpath");
            this.mTargetAlbumName = savedInstanceState.getString("key-targetfilename");
        }
        this.mContext = getActivity();
        this.mIsOutside = data.getBoolean("is_outside", false);
        this.mIsCloud = data.getBoolean("is-cloud", false);
        this.mAlbumDialog = new CreateAlbumDialog(this.mContext);
        this.mAlbumDialog.setListner(this);
        this.mMediaSet = getGalleryContext().getDataManager().getMediaSet(data.getString("media-path"));
        this.mAlbumSetDataLoader = new AlbumSetDataLoader(this.mContext, this.mMediaSet, 64);
        this.mSelectionManager = new SelectionManager(getGalleryContext(), true);
        this.mSelectionManager.setSelectionListener(this);
        if (this.mIsOutside) {
            this.mSelectionManager.setSourceMediaSet(getGalleryContext().getDataManager().getMediaSet(DataManager.getTopOutSideSetPathExcludeCloudAndRecycleAlbum()));
        } else {
            this.mSelectionManager.setSourceMediaSet(this.mMediaSet);
        }
        this.mAlbumSetListAdapter = new ListAlbumSetDataAdapter(getActivity(), this.mAlbumSetDataLoader, this.mSelectionManager, this.mIsOutside);
        this.mAlbumSetDataLoader.setLoadingListener(new MyLoadingListener());
        this.mDetailsSource = new MyDetailsSource();
        this.mIntentChooser = new IntentChooser(this.mContext);
        this.mPhotoShareItem = new PhotoShareItem(this.mContext);
        this.mMenuExecutor = new MenuExecutor(getGalleryContext(), this.mSelectionManager, this.mIntentChooser, null);
        boolean needLazyLoad = data.getBoolean("need-lazy-load", true);
        if (!this.mUserHaveFirstLook && needLazyLoad) {
            z = false;
        }
        this.mUserHaveFirstLook = z;
        this.mLongTapManager = new LongTapManager((AbstractGalleryActivity) getActivity());
        this.mLongTapManager.setListener(new OnItemClickedListener() {
            public boolean onItemClicked(int resId, int slotIndex) {
                switch (resId) {
                    case R.string.delete:
                        ActionDeleteAndConfirm deleteDialog = new ActionDeleteAndConfirm(ListAlbumSetFragment.this.getActivity(), null, ListAlbumSetFragment.this.getDeleteAlbumMsg());
                        deleteDialog.setOnClickListener(new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == -1) {
                                    ListAlbumSetFragment.this.deleteAutoUploadAlbum(ListAlbumSetFragment.this.mContext);
                                }
                            }
                        });
                        deleteDialog.updateStatus(ListAlbumSetFragment.this.isSyncAlbum(), ListAlbumSetFragment.this.isHicloudAlbum());
                        deleteDialog.show();
                        return true;
                    case R.string.details:
                        if (ListAlbumSetFragment.this.mShowDetails) {
                            ListAlbumSetFragment.this.hideDetails();
                        } else {
                            ListAlbumSetFragment.this.showDetails();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        TraceController.endSection();
    }

    private String getDeleteAlbumMsg() {
        if (RecycleUtils.supportRecycle()) {
            return String.format(getResources().getString(R.string.message_deletepopupwindow01), new Object[]{Integer.valueOf(30)});
        } else if (isHicloudAlbum()) {
            return getResources().getString(R.string.delete_cloud_album_title);
        } else {
            return getResources().getQuantityString(R.plurals.delete_selection, 1, new Object[]{Integer.valueOf(1)});
        }
    }

    private void deleteAutoUploadAlbum(final Context context) {
        if (this.mTargetMediaSet instanceof CloudLocalAlbum) {
            GalleryLog.d("Recycle_ListAlbumSetFrgmt", "deleteAutoUploadAlbum " + this.mTargetMediaSet.getAlbumInfo().getId());
            new Thread() {
                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    ListAlbumSetFragment.this.mHandler.postDelayed(ListAlbumSetFragment.this.mShowDeletingDialogRunnable, 500);
                    if (!RecycleUtils.supportRecycle()) {
                        try {
                            PhotoShareUtils.getServer().deleteGeneralAlbum(ListAlbumSetFragment.this.mTargetMediaSet.getAlbumInfo().getId());
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                        }
                    }
                    Closeable closeable = null;
                    ContentResolver resolver = context.getContentResolver();
                    StringBuffer sb = new StringBuffer();
                    try {
                        String[] galleryMediaProjection = new String[]{"local_media_id"};
                        String whereClause = "local_media_id !=-1 and cloud_bucket_id=?";
                        if (RecycleUtils.supportRecycle()) {
                            galleryMediaProjection = new String[]{"local_media_id", "_id", "media_type"};
                            whereClause = "cloud_bucket_id=?";
                        }
                        closeable = resolver.query(GalleryMedia.URI, galleryMediaProjection, whereClause, new String[]{ListAlbumSetFragment.this.mTargetMediaSet.getAlbumInfo().getId()}, null, null);
                        if (closeable != null) {
                            SparseIntArray idMediaTypeArray = new SparseIntArray();
                            while (closeable.moveToNext()) {
                                sb.append(", ").append(closeable.getInt(0));
                                if (RecycleUtils.supportRecycle()) {
                                    idMediaTypeArray.put(closeable.getInt(1), closeable.getInt(2));
                                }
                            }
                            if (idMediaTypeArray.size() > 0) {
                                RecycleUtils.recyclePhotoItem(ListAlbumSetFragment.this.getGalleryContext().getGalleryApplication().getDataManager(), idMediaTypeArray, ListAlbumSetFragment.this.mMenuExecutor);
                            }
                        }
                        RecycleUtils.startAsyncAlbumInfo();
                        Utils.closeSilently(closeable);
                    } catch (SQLiteException e2) {
                        GalleryLog.e("photoshareLogTag", "query Deleted local Image " + e2.getMessage());
                    } catch (Throwable th) {
                        RecycleUtils.startAsyncAlbumInfo();
                        Utils.closeSilently(closeable);
                    }
                    resolver.delete(GalleryMedia.URI, "cloud_bucket_id=?", new String[]{ListAlbumSetFragment.this.mTargetMediaSet.getAlbumInfo().getId()});
                    if (sb.length() > 0) {
                        resolver.delete(Files.getContentUri("external"), "_id in (" + sb.substring(1) + ")", null);
                    }
                    ContentValues values = new ContentValues();
                    values.put("deleteFlag", Integer.valueOf(1));
                    resolver.update(PhotoShareConstants.CLOUD_ALBUM_TABLE_URI, values, "albumId=?", new String[]{ListAlbumSetFragment.this.mTargetMediaSet.getAlbumInfo().getId()});
                    ListAlbumSetFragment.this.mHandler.removeCallbacks(ListAlbumSetFragment.this.mShowDeletingDialogRunnable);
                    ListAlbumSetFragment.this.mHandler.post(new Runnable() {
                        public void run() {
                            ListAlbumSetFragment.this.dismissProgressDialog();
                        }
                    });
                }
            }.start();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAlbumSetDataLoader.backupData();
        this.mAlbumDialog.hide();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int i;
        TraceController.beginSection("ListAlbumSetFragment.onCreateView");
        View view = inflater.inflate(R.layout.fragment_list_albumset, container, false);
        this.mAlbumSetList = (DragListView) view.findViewById(R.id.list_albumset);
        this.mAlbumSetListAdapter.setListView(this.mAlbumSetList);
        this.mEmptyListView = view.findViewById(R.id.list_empty);
        this.mLoadingTipView = view.findViewById(R.id.list_loading_tips);
        if (this.mIsCloud) {
            i = 2;
        } else {
            i = 0;
        }
        setWindowPadding(i | 256, view);
        View footerView = inflater.inflate(R.layout.blank_footer_view, this.mAlbumSetList, false);
        this.mAlbumSetList.setFooterDividersEnabled(false);
        this.mAlbumSetList.addFooterView(footerView, null, false);
        TraceController.endSection();
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        TraceController.beginSection("ListAlbumSetFragment.onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
        TraceController.endSection();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        TraceController.beginSection("ListAlbumSetFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        this.mAlbumSetList.setAdapter(this.mAlbumSetListAdapter);
        this.mAlbumSetList.setListDataSource(this.mAlbumSetDataLoader);
        this.mAlbumSetList.setOnItemLongClickListener(this);
        this.mAlbumSetList.setOnItemClickListener(this);
        TraceController.endSection();
    }

    protected void onCreateActionBar(Menu menu) {
        TraceController.beginSection("ListAlbumSetFragment.onCreate");
        if (this.mIsCloud) {
            requestFeature(258);
        } else {
            requestFeature(256);
        }
        if (this.mIsOutside) {
            this.mActionBar.enterTabMode(false).setMenu(2, Action.ADD_ALBUM, Action.HIDE, Action.SETTINGS);
        } else {
            ActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
            am.setBothAction(Action.NONE, Action.NONE);
            am.setTitle(this.mIsCloud ? R.string.hicloud_gallery_new : R.string.other_album);
            am.setMenu(1, Action.HIDE, Action.SETTINGS);
            am.show();
        }
        TraceController.endSection();
    }

    public void onResume() {
        TraceController.beginSection("ListAlbumSetFragment.onResume");
        GalleryLog.d("PF", "list album set fragment on resume start");
        super.onResume();
        this.mIsActive = true;
        getGalleryContext().getDataManager().notifyReload(Constant.RELOAD_URI_ALBUMSET, 6);
        if (this.mUserHaveFirstLook) {
            this.mAlbumSetDataLoader.resume();
        } else {
            this.mLoadingTipView.setVisibility(0);
        }
        this.mAlbumSetListAdapter.resume();
        this.mIntentChooser.resume();
        GalleryLog.d("PF", "list album set fragment on resume end");
        TraceController.endSection();
    }

    public void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mIntentChooser.hideIfShowing();
        this.mIntentChooser.pause();
        if (this.mShowDetails) {
            hideDetails();
        }
        this.mAlbumSetDataLoader.pause();
        this.mActionBar.getCurrentMode().hide();
        DetailsHelper.pause();
        if (this.mDeleteDialog != null) {
            this.mDeleteDialog.dismiss();
        }
    }

    public void onActionItemClicked(Action action) {
        GalleryLog.d("ListAlbumSetFragment", "id = " + action);
        ReportToBigData.reportActionForFragment("FromListView", action, this.mSelectionManager);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                if (!PermissionManager.requestPermissionsIfNeed(getActivity(), PermissionManager.getPermissionsStorage(), 1000)) {
                    showNewAlbumDialog(this.mContext, this.mAlbumDialog);
                    break;
                }
                return;
            case 2:
            case 3:
                if (!this.mSelectionManager.inSelectAllMode()) {
                    this.mSelectionManager.selectAll();
                    break;
                } else {
                    this.mSelectionManager.deSelectAll();
                    break;
                }
            case 4:
                String delMsg;
                int itemCount = this.mSelectionManager.getItemSelectCount();
                if (this.mSelectionManager.getMediasetIfSelectedOnlyOne() instanceof VirtualAlbum) {
                    delMsg = getResources().getString(R.string.delete_virtual_multi_msg);
                } else {
                    delMsg = getResources().getQuantityString(R.plurals.delete_selection, itemCount, new Object[]{Integer.valueOf(itemCount)});
                }
                final Bundle bundle = new Bundle();
                bundle.putInt("recycle_flag", RecycleUtils.supportRecycle() ? 2 : 0);
                if (RecycleUtils.supportRecycle()) {
                    if (PhotoShareUtils.isHiCloudLoginAndCloudPhotoSwitchOpen()) {
                        delMsg = getResources().getQuantityString(R.plurals.delete_synced_photo_msg, itemCount, new Object[]{Integer.valueOf(30)});
                    } else {
                        delMsg = getResources().getQuantityString(R.plurals.delete_local_photo_msg, itemCount, new Object[]{Integer.valueOf(30)});
                    }
                }
                if (this.mDeleteDialog != null) {
                    this.mDeleteDialog.show(delMsg);
                    break;
                }
                if (RecycleUtils.supportRecycle()) {
                    this.mDeleteDialog = new ActionRecycleAndConfirm(getActivity(), null, delMsg);
                } else {
                    this.mDeleteDialog = new ActionDeleteAndConfirm(getActivity(), delMsg);
                }
                this.mDeleteDialog.setOnClickListener(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == -1) {
                            ListAlbumSetFragment.this.mMenuExecutor.startAction(R.string.delete, R.string.delete, null, false, true, Style.NORMAL_STYLE, null, bundle, 1);
                            ReportToBigData.report(107);
                        }
                    }
                });
                this.mDeleteDialog.show();
                break;
            case 5:
                if (!this.mShowDetails) {
                    showDetails();
                    break;
                } else {
                    hideDetails();
                    break;
                }
            case 6:
                Bundle data = new Bundle();
                data.putString("media-path", getGalleryContext().getDataManager().getTopSetPath(8388608));
                ReportToBigData.report(45, "");
                Intent intent = new Intent(this.mContext, ListAlbumHiddenActivity.class);
                intent.putExtras(data);
                startActivity(intent);
                break;
            case 8:
                this.mMenuExecutor.startAction(R.string.move_in, R.string.move_in, null);
                ReportToBigData.report(19, String.format("{MoveAlbum:%s}", new Object[]{"MoveIn"}));
                break;
            case 9:
                this.mMenuExecutor.startAction(R.string.move_out, R.string.move_out, null);
                ReportToBigData.report(19, String.format("{MoveAlbum:%s}", new Object[]{"MoveOut"}));
                break;
            case 10:
                if (this.mSelectionManager.inSelectionMode() && !this.mIsDragging) {
                    this.mSelectionManager.leaveSelectionMode();
                    break;
                }
            case 12:
                photoShareBackUp();
                break;
            case 13:
                this.mAlbumDialog.showForRename(this.mSelectionManager.getSelectedAlbumName(), R.string.rename, this.mSelectionManager.getSelectedAlbumPath());
                break;
            case 14:
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ListAlbumSetFragment.this.mContext.startActivity(new Intent(ListAlbumSetFragment.this.mContext, GallerySettings.class));
                    }
                }, 150);
                break;
            case 15:
                if (this.mSelectionManager.getItemSelectCount() <= 500) {
                    if (PhotoShareUtils.isSupportShareToCloud()) {
                        this.mIntentChooser.addShareItem(this.mPhotoShareItem);
                    } else {
                        this.mIntentChooser.removeShareItem(this.mPhotoShareItem);
                    }
                    this.mIntentChooser.share(getGalleryContext(), this.mActionBar.getCurrentMode(), this.mMenuExecutor, null, this.mSelectionManager.getSelected(true));
                    break;
                }
                GalleryUtils.showLimitExceedDialog(getActivity());
                return;
        }
    }

    private void photoShareBackUp() {
        if (PhotoShareUtils.isNetworkConnected(getActivity())) {
            String name = getGalleryContext().getDataManager().getMediaSet((Path) this.mSelectionManager.getSelected(false).get(0)).getName();
            if ("default-album-2".equals(name) || "default-album-1".equals(name)) {
                ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_toast_album_already_cloud, 0);
                return;
            }
            if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isCloudPhotoSwitchOpen()) {
                doRealBackUp();
            } else {
                Intent intent = new Intent(getActivity(), PhotoShareLoginActivity.class);
                intent.putExtra("needPhotoshareOpen", true);
                this.mContext.startActivityForResult(intent, 1);
            }
            return;
        }
        ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
    }

    private void doRealBackUp() {
        final MediaSet mediaSet = getGalleryContext().getDataManager().getMediaSet((Path) this.mSelectionManager.getSelected(false).get(0));
        final String name = mediaSet.getName();
        final ShareInfo shareToAdd = PhotoShareUtils.getShareInfo(name);
        if (shareToAdd != null) {
            PhotoShareUtils.getPhotoShareDialog(getActivity(), (int) R.string.dialog_photoshare_addtoCloud_title, (int) R.string.dialog_photoshare_addtoCloud_positive_button, (int) R.string.cancel, getActivity().getString(R.string.dialog_photoshare_addtoCloud_content), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (-1 == which) {
                        ListAlbumSetFragment.this.showProgressDialog(ListAlbumSetFragment.this.getActivity().getString(R.string.progress_text_now_backingup));
                        final MediaSet mediaSet = mediaSet;
                        final ShareInfo shareInfo = shareToAdd;
                        final String str = name;
                        new Thread() {
                            public void run() {
                                try {
                                    ArrayList<String> filePath = ListAlbumSetFragment.this.getFilePath(mediaSet);
                                    ArrayList<String> fileNeedToAdd = PhotoShareUtils.checkMd5ExistsInShare(shareInfo.getShareId(), filePath);
                                    if (filePath.size() > fileNeedToAdd.size()) {
                                        PhotoShareUtils.showFileExitsTips(filePath.size() - fileNeedToAdd.size());
                                    }
                                    if (fileNeedToAdd.isEmpty()) {
                                        ListAlbumSetFragment.this.backUpSuccess(str);
                                        return;
                                    }
                                    int result = PhotoShareUtils.getServer().addFileToShare(shareInfo.getShareId(), (String[]) fileNeedToAdd.toArray(new String[fileNeedToAdd.size()]));
                                    GalleryLog.v("ListAlbumSetFragment", "addFileToShare result " + result);
                                    if (result != 0) {
                                        ListAlbumSetFragment.this.showBackUpFail();
                                    } else {
                                        ListAlbumSetFragment.this.backUpSuccess(str);
                                    }
                                } catch (RemoteException e) {
                                    ListAlbumSetFragment.this.showBackUpFail();
                                    PhotoShareUtils.dealRemoteException(e);
                                }
                            }
                        }.start();
                    }
                }
            }).show();
            return;
        }
        showProgressDialog(getActivity().getString(R.string.progress_text_now_backingup));
        new Thread() {
            public void run() {
                try {
                    ShareInfo shareInfo = new ShareInfo();
                    shareInfo.setShareName(name);
                    ArrayList<String> filePath = ListAlbumSetFragment.this.getFilePath(mediaSet);
                    ArrayList<String> fileNeedToAdd = PhotoShareUtils.checkMd5ExistsWhenCreateNewShare(filePath);
                    if (filePath.size() > fileNeedToAdd.size()) {
                        PhotoShareUtils.showSameFileTips(filePath.size() - fileNeedToAdd.size());
                    }
                    int result = 0;
                    try {
                        result = PhotoShareUtils.getServer().createShare(shareInfo, (String[]) fileNeedToAdd.toArray(new String[fileNeedToAdd.size()]));
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                    GalleryLog.v("ListAlbumSetFragment", "createShare result " + result);
                    if (result == 1005) {
                        new Handler(ListAlbumSetFragment.this.getActivity().getMainLooper()).post(new Runnable() {
                            public void run() {
                                ListAlbumSetFragment.this.dismissProgressDialog();
                                ListAlbumSetFragment.this.doRealBackUp();
                            }
                        });
                        PhotoShareUtils.getServer().refreshShare();
                    } else if (result != 0) {
                        ListAlbumSetFragment.this.showBackUpFail();
                    } else {
                        ListAlbumSetFragment.this.backUpSuccess(name);
                    }
                } catch (RemoteException e2) {
                    ListAlbumSetFragment.this.showBackUpFail();
                    PhotoShareUtils.dealRemoteException(e2);
                }
            }
        }.start();
    }

    private ArrayList<String> getFilePath(MediaSet mediaSet) {
        ArrayList<String> filePath = new ArrayList();
        for (MediaItem item : mediaSet.getMediaItem(0, mediaSet.getMediaItemCount())) {
            if (item.getFilePath() != null) {
                filePath.add(item.getFilePath());
            }
        }
        return filePath;
    }

    private void backUpSuccess(String name) {
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            public void run() {
                ListAlbumSetFragment.this.dismissProgressDialog();
                ListAlbumSetFragment.this.mSelectionManager.leaveSelectionMode();
                PhotoShareUtils.enableUploadStatusBarNotification(true);
                PhotoShareUtils.refreshStatusBar(false);
            }
        });
    }

    private void showBackUpFail() {
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            public void run() {
                ListAlbumSetFragment.this.dismissProgressDialog();
                ListAlbumSetFragment.this.mSelectionManager.leaveSelectionMode();
                ContextedUtils.showToastQuickly(ListAlbumSetFragment.this.getActivity(), (int) R.string.toast_gallerycloud_backup_failed, 0);
            }
        });
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        onSingleTapUp(position);
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        MediaSet targetSet = this.mAlbumSetDataLoader.getMediaSet(position);
        if (targetSet == null) {
            return true;
        }
        String targetSetLabel = targetSet.getLabel();
        if ("photoshare".equalsIgnoreCase(targetSetLabel) || "recycle".equalsIgnoreCase(targetSetLabel)) {
            return true;
        }
        this.mLongTapSlotIndex = position;
        if (this.mIsCloud) {
            this.mLongTapManager.show(targetSet, position);
            this.mTargetMediaSet = targetSet;
            return true;
        }
        this.mSelectionManager.setAutoLeaveSelectionMode(false);
        this.mSelectionManager.enterSelectionMode();
        this.mSelectionManager.toggle(targetSet.getPath());
        if (this.mSelectionManager.inSelectionMode()) {
            if (!targetSet.isVirtual() && this.mSelectionManager.isItemSelected(targetSet.getPath())) {
                this.mSelectedPositions.add(Integer.valueOf(position));
            } else if (this.mSelectedPositions.contains(Integer.valueOf(position))) {
                this.mSelectedPositions.remove(Integer.valueOf(position));
            }
        }
        return true;
    }

    public void onSelectionChange(Path path, boolean selected) {
        updateSelectTitile(this.mSelectionMode);
        updateActionMenu(this.mSelectionMode);
        this.mAlbumSetListAdapter.notifyDataSetChanged();
        if (this.mSelectionManager.getSelectedCount() != this.mSelectionManager.getTotalCount()) {
            this.mSelectionMode.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        }
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                this.mAlbumSetList.setMultiSelectEnable(true);
                this.mSelectionMode = this.mActionBar.enterSelectionMode(true);
                this.mSelectionMode.setLeftAction(Action.NO);
                if (this.mIsOutside) {
                    this.mSelectionMode.setMenu(5, Action.SHARE, Action.MOVEIN, Action.DEL, Action.ALL, Action.RENAME, Action.DETAIL);
                } else {
                    this.mSelectionMode.setMenu(5, Action.SHARE, Action.MOVEOUT, Action.DEL, Action.ALL, Action.RENAME, Action.DETAIL);
                }
                updateSelectTitile(this.mSelectionMode);
                updateActionMenu(this.mSelectionMode);
                this.mSelectionMode.show();
                if (this.mIsOutside) {
                    this.mAlbumSetList.setDropListener(this.mDropListener);
                }
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                return;
            case 2:
                this.mAlbumSetList.setMultiSelectEnable(false);
                if (this.mIsOutside) {
                    if (this.mIsDragSortUsed) {
                        this.mIsDragSortUsed = false;
                        ReportToBigData.report(18);
                    }
                    this.mAlbumSetList.setDropListener(null);
                }
                this.mActionBar.leaveCurrentMode();
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                this.mSelectedPositions.clear();
                return;
            case 3:
                updateSelectTitile(this.mSelectionMode);
                updateActionMenu(this.mSelectionMode);
                this.mSelectionMode.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                break;
            case 4:
                updateSelectTitile(this.mSelectionMode);
                updateActionMenu(this.mSelectionMode);
                this.mSelectionMode.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                break;
        }
    }

    protected void onSingleTapUp(int position) {
        boolean z = true;
        MediaSet targetSet = this.mAlbumSetDataLoader.getMediaSet(position);
        if (targetSet != null) {
            if (this.mSelectionManager.inSelectionMode()) {
                String targetSetLabel = targetSet.getLabel();
                if (!"photoshare".equalsIgnoreCase(targetSetLabel) && !"recycle".equalsIgnoreCase(targetSetLabel)) {
                    this.mSelectionManager.toggle(targetSet.getPath());
                    if (!targetSet.isVirtual() && this.mSelectionManager.isItemSelected(targetSet.getPath())) {
                        this.mSelectedPositions.add(Integer.valueOf(position));
                    } else if (this.mSelectedPositions.contains(Integer.valueOf(position))) {
                        this.mSelectedPositions.remove(Integer.valueOf(position));
                    }
                }
            } else if (targetSet.isVirtual() && "other".equalsIgnoreCase(targetSet.getLabel())) {
                data = new Bundle();
                data.putString("media-path", getGalleryContext().getDataManager().getTopSetPath(262144));
                intent = new Intent(this.mContext, ListAlbumSetActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            } else if (targetSet.isVirtual() && "photoshare".equalsIgnoreCase(targetSet.getLabel())) {
                RefreshHelper.refreshAlbum(System.currentTimeMillis());
                ReportToBigData.report(SmsCheckResult.ESCT_163);
                data = new Bundle();
                data.putString("media-path", "/photoshare/local");
                intent = new Intent(this.mContext, ListAlbumSetActivity.class);
                data.putBoolean("is-cloud", true);
                intent.putExtras(data);
                startActivity(intent);
            } else {
                data = new Bundle();
                intent = new Intent();
                if (this.mIsCloud) {
                    data.putBoolean("get-content", false);
                    data.putString("media-path", targetSet.getPath().toString());
                    intent.setClass(getActivity(), PhotoShareAlbumActivity.class);
                } else {
                    data.putString("media-path", targetSet.getPath().toString());
                    String str = "only-local-camera-video-album";
                    if (!targetSet.isVirtual()) {
                        z = false;
                    } else if (!"camera_video".equalsIgnoreCase(targetSet.getLabel())) {
                        z = "screenshots_video".equalsIgnoreCase(targetSet.getLabel());
                    }
                    data.putBoolean(str, z);
                    if (targetSet instanceof GalleryRecycleAlbum) {
                        intent.setClass(getActivity(), RecycleAlbumActivity.class);
                    } else {
                        intent.setClass(getActivity(), SlotAlbumActivity.class);
                    }
                }
                intent.putExtras(data);
                startActivity(intent);
            }
        }
    }

    public void onSelectionLimitExceed() {
    }

    protected void updateSelectTitile(SelectionMode mode) {
        if (mode != null) {
            int count = this.mSelectionManager.getSelectedCount();
            if (count == 0) {
                mode.setTitle(this.mContext.getString(R.string.no_selected));
                mode.setCount(null);
                return;
            }
            mode.setTitle(this.mContext.getString(R.string.has_selected));
            mode.setCount(count);
        }
    }

    protected void updateActionMenu(AbstractTitleMode mode) {
        if (mode != null) {
            MenuEnableCtrller.updateMenuOperationForList(mode, getSelectedSupportOperations(), this.mSelectionManager);
        }
    }

    private int getSelectedSupportOperations() {
        List<Path> list = this.mSelectionManager.getSelected(false);
        Map<String, Path> supportMap = new HashMap();
        for (Path path : list) {
            String albumType = path.getSuffix();
            if ("mtp".equalsIgnoreCase(path.getPrefix())) {
                albumType = "mtp";
            } else if (GalleryUtils.isPathSuffixInteger(albumType)) {
                albumType = "local";
            }
            if (!supportMap.containsKey(albumType)) {
                supportMap.put(albumType, path);
            }
        }
        int supportOperation = supportMap.isEmpty() ? 0 : -1;
        DataManager dataManager = getGalleryContext().getDataManager();
        for (Path path2 : supportMap.values()) {
            supportOperation &= dataManager.getMediaSet(path2).getSupportedOperations();
        }
        return supportOperation;
    }

    protected void hideDetails() {
        this.mShowDetails = false;
        this.mDetailsHelper.hide();
    }

    private void showDetails() {
        this.mShowDetails = true;
        if (this.mDetailsHelper == null) {
            this.mDetailsHelper = new DetailsHelper(getGalleryContext(), null, this.mDetailsSource);
            this.mDetailsHelper.setCloseListener(new CloseListener() {
                public void onClose() {
                    GalleryLog.printDFXLog("hideDetails");
                    ListAlbumSetFragment.this.hideDetails();
                }
            });
        }
        this.mDetailsHelper.show();
    }

    public boolean onBackPressed() {
        if (this.mShowDetails) {
            hideDetails();
            return true;
        } else if (this.mSelectionManager.inSelectionMode()) {
            if (!this.mIsDragging) {
                this.mSelectionManager.leaveSelectionMode();
            }
            return true;
        } else {
            ReportToBigData.report(37, String.format("{ExitGalleryView:%s}", new Object[]{"FromListView"}));
            return false;
        }
    }

    private void exchangeSortIndex(MediaSet fromMediaSet, MediaSet toMediaSet) {
        if (fromMediaSet != null && toMediaSet != null) {
            int fromBucketId = fromMediaSet.getBucketId();
            int toBucketId = toMediaSet.getBucketId();
            if (fromBucketId != 0 && toBucketId != 0) {
                getGalleryContext().getGalleryApplication().getGalleryData().exchangeAlbumIndex(fromBucketId, toBucketId);
            }
        }
    }

    public void onFinish(boolean created, String dir, String name) {
        if (created && dir == null) {
            if (this.mSelectionManager.inSelectionMode()) {
                String bucketPath = getGalleryContext().getDataManager().getMediaSet((Path) this.mSelectionManager.getSelected(false).get(0)).getBucketPath();
                if (!(bucketPath.startsWith(BucketHelper.PRE_LOADED_PATH_PREFIX) || GalleryUtils.hasSpaceForSize(UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST, bucketPath))) {
                    this.mAlbumDialog.hide();
                    this.mSelectionManager.leaveSelectionMode();
                    ContextedUtils.showToastQuickly(this.mContext, this.mContext.getString(R.string.insufficient_storage_space), 0);
                    return;
                }
            }
            Bundle data = new Bundle();
            data.putString("key_bucket_name_alias", name);
            this.mMenuExecutor.startAction(R.string.rename, R.string.rename, this.mRenameProcessListener, false, true, Style.NORMAL_STYLE, this.mSelectionManager.getProcessingList(false), data);
            ReportToBigData.report(106);
        } else if (created && dir != null) {
            this.mTargetPath = dir;
            this.mTargetAlbumName = name;
            if (this.mAlbumSetDataLoader.hasAnyItem()) {
                goToChoosePhotos();
            }
            ReportToBigData.report(17);
        }
    }

    private void goToChoosePhotos() {
        Intent intent = new Intent(getActivity(), ListAlbumPickerActivity.class).setAction("android.intent.action.GET_CONTENT").setType("*/*");
        intent.putExtra("support-multipick-items", true);
        this.mContext.startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == -1) {
                    ArrayList<String> result = data.getStringArrayListExtra("select-item-list");
                    if (result != null && !result.isEmpty()) {
                        this.mPasteList = new ArrayList();
                        for (String path : result) {
                            this.mPasteList.add(Path.fromString(path));
                        }
                        new Builder(this.mContext).setTitle(R.string.select_add_type).setSingleChoiceItems(new ArrayAdapter(this.mContext, R.layout.simple_list_item_single_choice, getResources().getStringArray(R.array.choose_add_types)), 0, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle = new Bundle();
                                bundle.putString("key-targetpath", ListAlbumSetFragment.this.mTargetPath);
                                bundle.putString("key-targetfilename", ListAlbumSetFragment.this.mTargetAlbumName);
                                bundle.putInt("key-pastestate", ListAlbumSetFragment.this.PASTE_TYPES[which]);
                                bundle.putBoolean("key-customprogress", true);
                                ListAlbumSetFragment.this.mMenuExecutor.startAction(R.id.action_paste, R.string.paste, ListAlbumSetFragment.this.mPasteProcessListener, false, true, Style.PASTE_STYLE, ListAlbumSetFragment.this.mPasteList, bundle);
                                GalleryUtils.dismissDialogSafely(dialog, ListAlbumSetFragment.this.mContext);
                                String str = "{AddType:%s}";
                                Object[] objArr = new Object[1];
                                objArr[0] = ListAlbumSetFragment.this.PASTE_TYPES[which] == 1 ? "Copy" : "Move";
                                ReportToBigData.report(101, String.format(str, objArr));
                            }
                        }).setNegativeButton(this.mContext.getString(R.string.cancel), null).create().show();
                        break;
                    }
                    return;
                }
                break;
            case 1:
                if (resultCode == -1) {
                    doRealBackUp();
                    break;
                }
                break;
            case 500:
                this.mIntentChooser.onReceiveShareResult(requestCode, resultCode, data);
                break;
        }
    }

    public void dialogDismiss() {
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setWindowPadding((this.mIsCloud ? 2 : 0) | 256);
        updateEmptyLayoutPadding(getActivity(), this.mEmptyListView);
    }

    public static void updateEmptyLayoutPadding(Context context, View emptyView) {
        if (emptyView != null && emptyView.getVisibility() == 0) {
            Resources res = context.getResources();
            if (MultiWindowStatusHolder.isInMultiWindowMode()) {
                emptyView.setPadding(0, 0, 0, 0);
                ((LinearLayout) emptyView).setGravity(17);
                return;
            }
            emptyView.setPadding(0, ((LayoutHelper.isPort() ? GalleryUtils.getHeightPixels() : GalleryUtils.getWidthPixels()) * res.getInteger(R.integer.list_empty_album_top_padding_numerator)) / res.getInteger(R.integer.list_empty_album_top_padding_denominator), 0, res.getDimensionPixelSize(R.dimen.list_empty_album_bottom_padding));
            ((LinearLayout) emptyView).setGravity(1);
        }
    }

    private void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(getActivity());
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.show();
        GalleryLog.printDFXLog("showProgressDialog log for DFX");
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    protected void onUserSelected(boolean selected) {
        if (selected) {
            if (this.mAlbumSetDataLoader != null) {
                this.mAlbumSetDataLoader.unfreeze();
                if (this.mIsActive) {
                    this.mAlbumSetDataLoader.resume();
                }
            }
        } else if (this.mAlbumSetDataLoader != null) {
            this.mAlbumSetDataLoader.freeze();
        }
        super.onUserSelected(selected);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("key-targetpath", this.mTargetPath);
        outState.putString("key-targetfilename", this.mTargetAlbumName);
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return true;
    }

    protected void relayoutIfNeed() {
        updateEmptyLayoutPadding(this.mContext, this.mEmptyListView);
    }

    protected boolean isSyncAlbum() {
        return false;
    }

    protected boolean isHicloudAlbum() {
        return this.mMediaSet instanceof CloudLocalAlbumSet;
    }

    public static void showNewAlbumDialog(Activity context, CreateAlbumDialog albumDialog) {
        if (GalleryStorageManager.getInstance().hasAnyMountedOuterGalleryStorage()) {
            albumDialog.showWithSpaceInfo();
            return;
        }
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null) {
            albumDialog.show(GalleryUtils.getDefualtAlbumName(context, innerGalleryStorage.getPath() + File.separator + "Pictures"), R.string.new_album, true);
        }
    }
}
