package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareDownUpAlbum;
import com.android.gallery3d.data.PhotoShareDownUpItem;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SelectionManager.SelectionListener;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.app.PhotoShareDownUpDataAdapter.ClickListener;
import com.huawei.gallery.app.PhotoShareDownUpSlidingWindow.ItemsEntry;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.LayoutHelper;
import java.util.ArrayList;

public class PhotoShareDownUpFragment extends AbstractGalleryFragment implements ClickListener, SelectionListener, OnItemLongClickListener, OnItemClickListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Action[] DOWNLOADING_MENU = new Action[]{Action.REMOVE, Action.PHOTOSHARE_PAUSE, Action.PHOTOSHARE_DOWNLOAD_START, Action.ALL};
    private static final Action[] FINISHED_MENU = new Action[]{Action.PHOTOSHARE_CLEAR, Action.ALL};
    private static final Action[] UPLOADING_MENU = new Action[]{Action.REMOVE, Action.PHOTOSHARE_PAUSE, Action.PHOTOSHARE_UPLOAD_START, Action.ALL};
    private GalleryActionBar mActionBar;
    private AbstractGalleryActivity mContext;
    private PhotoShareDownUpDataLoader mDataLoader;
    private OnClickListener mDeleteClickListener;
    private ProgressDialog mDialog;
    private TextView mEmptyTipsView;
    private View mEmptyView;
    private SelectionManager mSelectionManager;
    private SelectionMode mSelectionMode;
    private PhotoShareDownUpSlidingWindow mSlidingWindow;
    private PhotoShareDownUpAlbum mSource;
    private int mType;
    private PhotoShareDownUpDataAdapter mViewAdapter;

    private class DataLoaderListener implements LoadingListener {
        private DataLoaderListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            if (PhotoShareDownUpFragment.this.mEmptyView != null) {
                if (PhotoShareDownUpFragment.this.mDataLoader.size() == 0) {
                    PhotoShareDownUpFragment.this.mEmptyView.setVisibility(0);
                    PhotoShareDownUpFragment.this.updateEmptyViewPaddingTop();
                    PhotoShareDownUpFragment.this.setEmptyTips();
                } else {
                    PhotoShareDownUpFragment.this.mEmptyView.setVisibility(8);
                }
            }
        }

        public void onVisibleRangeLoadFinished() {
        }
    }

    private class Operation extends BaseJob<Void> {
        private Handler handler = new Handler(PhotoShareDownUpFragment.this.mContext.getMainLooper());
        private final Action mOperation;

        public Operation(Action operation) {
            this.mOperation = operation;
        }

        public Void run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            PhotoShareDownUpFragment.this.fileInfoDetailBatchOperation(this.mOperation, PhotoShareDownUpFragment.this.getSelect());
            this.handler.post(new Runnable() {
                public void run() {
                    PhotoShareDownUpFragment.this.selectionManagerLeaveSelectionMode();
                    GalleryUtils.dismissDialogSafely(PhotoShareDownUpFragment.this.mDialog, PhotoShareDownUpFragment.this.mContext);
                    PhotoShareDownUpFragment.this.mDialog = null;
                }
            });
            return null;
        }

        public String workContent() {
            return "DownUpOperation";
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 9;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 10;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 12;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 14;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 15;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 16;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 17;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 18;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 2;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 19;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 20;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 21;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 22;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 23;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 24;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 25;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 26;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 27;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 28;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 29;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 30;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 31;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 32;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 33;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 34;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 35;
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
            iArr[Action.NO.ordinal()] = 3;
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
            iArr[Action.OK.ordinal()] = 43;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 44;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 45;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 46;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 47;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 48;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 49;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 50;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 51;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 52;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 53;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 54;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 5;
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
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 6;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 64;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 65;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 66;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 7;
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
            iArr[Action.REMOVE.ordinal()] = 8;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 73;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 74;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 75;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 76;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 77;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data != null) {
            this.mType = data.getInt("photo_share_down_up_fragment_state", 0);
        }
        this.mContext = (AbstractGalleryActivity) getActivity();
        this.mSource = (PhotoShareDownUpAlbum) this.mContext.getDataManager().getMediaSet(Path.fromString(getSourcePath(this.mType)));
        this.mDataLoader = new PhotoShareDownUpDataLoader(getActivity(), this.mSource);
        this.mDataLoader.setLoadListener(new DataLoaderListener());
        this.mSlidingWindow = new PhotoShareDownUpSlidingWindow(getActivity(), this.mDataLoader);
        this.mSelectionManager = new SelectionManager(this.mContext, false);
        this.mSelectionManager.setSelectionListener(this);
        this.mSelectionManager.setSourceMediaSet(this.mSource);
        this.mViewAdapter = new PhotoShareDownUpDataAdapter(getActivity(), this.mSlidingWindow, this.mSelectionManager);
        this.mViewAdapter.setListener(this);
        this.mDeleteClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareDownUpFragment.this.mDialog = PhotoShareDownUpFragment.this.createProgressDialog(PhotoShareDownUpFragment.this.getActivity(), R.string.please_wait);
                    PhotoShareDownUpFragment.this.mDialog.show();
                    PhotoShareDownUpFragment.this.execute(Action.REMOVE);
                }
            }
        };
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    public void onResume() {
        super.onResume();
        this.mDataLoader.resume();
        this.mSlidingWindow.resume();
        updateMainViewPadding();
    }

    public void onPause() {
        super.onPause();
        this.mDataLoader.pause();
        this.mSlidingWindow.pause();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        updateMainViewPadding();
        updateEmptyViewPaddingTop();
    }

    private void updateEmptyViewPaddingTop() {
        if (getActivity() != null && this.mEmptyView != null && this.mEmptyView.getVisibility() != 8) {
            this.mEmptyView.setPadding(0, ((LayoutHelper.isPort() ? GalleryUtils.getHeightPixels() : GalleryUtils.getWidthPixels()) * getActivity().getResources().getInteger(R.integer.photoshare_upload_empty_top_padding_numerator)) / getActivity().getResources().getInteger(R.integer.empty_album_top_padding_denominator), 0, 0);
        }
    }

    private void setEmptyTips() {
        if (this.mEmptyTipsView != null) {
            int resId;
            switch (this.mType) {
                case 1:
                    resId = R.string.photoshare_empty_downloading;
                    break;
                case 2:
                    resId = R.string.photoshare_empty_downloaded;
                    break;
                case 3:
                    resId = R.string.photoshare_empty_uploading;
                    break;
                case 4:
                    resId = R.string.photoshare_empty_uploaded;
                    break;
                default:
                    resId = 0;
                    break;
            }
            this.mEmptyTipsView.setText(resId);
        }
    }

    private ArrayList<FileInfoDetail> getSelect() {
        ArrayList<Path> selected = this.mSelectionManager.getSelected(false);
        ArrayList<FileInfoDetail> result = new ArrayList();
        if (selected.size() <= 0) {
            return result;
        }
        for (int i = 0; i < selected.size(); i++) {
            PhotoShareDownUpItem item = (PhotoShareDownUpItem) this.mContext.getDataManager().getMediaObject((Path) selected.get(i));
            if (item != null) {
                result.add(item.mFileInfo);
            }
        }
        return result;
    }

    public void selectionManagerLeaveSelectionMode() {
        if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.leaveSelectionMode();
        }
    }

    public void onActionItemClicked(Action action) {
        Activity activity = getActivity();
        if (activity != null) {
            switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
                case 1:
                case 2:
                    if (!this.mSelectionManager.inSelectAllMode()) {
                        this.mSelectionManager.selectAll();
                        break;
                    } else {
                        this.mSelectionManager.deSelectAll();
                        break;
                    }
                case 3:
                    selectionManagerLeaveSelectionMode();
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                    this.mDialog = createProgressDialog(activity, R.string.please_wait);
                    this.mDialog.show();
                    execute(action);
                    break;
                case 8:
                    showDeleteAlertDialog();
                    break;
            }
        }
    }

    private void showDeleteAlertDialog() {
        String delMsg;
        int totalCount = this.mSelectionManager.getTotalCount();
        int selectCount = this.mSelectionManager.getSelectedCount();
        if (totalCount <= 1 || selectCount != totalCount) {
            delMsg = getResources().getQuantityString(R.plurals.photoshare_delete_task_num_tips, selectCount, new Object[]{Integer.valueOf(selectCount)});
        } else {
            delMsg = getResources().getString(R.string.photoshare_delete_task_all_tips);
        }
        GalleryUtils.setTextColor(new Builder(getActivity()).setTitle(delMsg).setPositiveButton(R.string.action_remove_title, this.mDeleteClickListener).setNegativeButton(R.string.cancel, this.mDeleteClickListener).show().getButton(-1), getActivity().getResources());
    }

    private void execute(Action action) {
        this.mContext.getThreadPool().submit(new Operation(action));
    }

    public void onClick(int position, int state) {
        ItemsEntry entry = this.mSlidingWindow.getItem(position);
        if (entry != null && entry.mediaItem != null) {
            PhotoShareDownUpItem item = entry.mediaItem;
            if (1 == state || 2 == state) {
                item.cancel();
                PhotoShareUtils.updateNotify();
                PhotoShareUtils.refreshStatusBar(item.isDownLoad());
                return;
            }
            if (!(8 == state || 4 == state)) {
                if (32 != state) {
                    return;
                }
            }
            item.start();
            PhotoShareUtils.updateNotify();
            PhotoShareUtils.refreshStatusBar(item.isDownLoad());
        }
    }

    public void updateTitle() {
        if (getActivity() != null) {
            ((PhotoShareDownUpBaseActivity) getActivity()).updateTitle(getTitle(getActivity(), this.mType, this.mViewAdapter.getCount()), this.mType);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photoshare_down_up_fragment, container, false);
        ListView listView = (ListView) view.findViewById(R.id.content);
        listView.setAdapter(this.mViewAdapter);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        this.mViewAdapter.setView(listView);
        this.mEmptyView = view.findViewById(R.id.upload_empty);
        this.mEmptyTipsView = (TextView) view.findViewById(R.id.text_upload_empty);
        ((PhotoShareDownUpBaseActivity) getActivity()).updateTitle(getTitle(getActivity(), this.mType, this.mViewAdapter.getCount()), this.mType);
        View footerView = inflater.inflate(R.layout.blank_footer_view, listView, false);
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(footerView, null, false);
        return view;
    }

    protected void onCreateActionBar(Menu menu) {
        this.mActionBar.enterStandardTitleActionMode(false).show();
        getGalleryActionBar().setActionPanelVisible(false);
    }

    private void enterSelectionMode() {
        this.mSelectionMode = this.mActionBar.enterSelectionMode(true);
        this.mSelectionMode.setLeftAction(Action.NO);
        if (this.mType == 1) {
            this.mSelectionMode.setMenu(Math.min(5, DOWNLOADING_MENU.length), DOWNLOADING_MENU);
        } else if (this.mType == 3) {
            this.mSelectionMode.setMenu(Math.min(5, UPLOADING_MENU.length), UPLOADING_MENU);
        } else {
            this.mSelectionMode.setMenu(Math.min(5, FINISHED_MENU.length), FINISHED_MENU);
        }
        updateSelectTitle(this.mSelectionMode);
        this.mSelectionMode.show();
        getGalleryActionBar().setActionPanelVisible(true);
    }

    protected void updateSelectTitle(SelectionMode mode) {
        if (mode != null && getActivity() != null) {
            int count = this.mSelectionManager.getSelectedCount();
            ActionBarStateBase currentMode = this.mActionBar.getCurrentMode();
            if (count == 0) {
                mode.setTitle(getActivity().getString(R.string.no_selected));
                mode.setCount(null);
                currentMode.setActionEnable(false, Action.ACTION_ID_REMOVE);
                currentMode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_PAUSE);
                currentMode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_DOWNLOAD_START);
                currentMode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_UPLOAD_START);
                currentMode.setActionEnable(false, Action.ACTION_ID_PHOTOSHARE_CLEAR);
                return;
            }
            mode.setTitle(getActivity().getString(R.string.has_selected));
            mode.setCount(count);
            currentMode.setActionEnable(true, Action.ACTION_ID_REMOVE);
            currentMode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_PAUSE);
            currentMode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_DOWNLOAD_START);
            currentMode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_UPLOAD_START);
            currentMode.setActionEnable(true, Action.ACTION_ID_PHOTOSHARE_CLEAR);
        }
    }

    private void leaveSelectionMode() {
        this.mActionBar.leaveCurrentMode();
        getGalleryActionBar().setActionPanelVisible(false);
    }

    public boolean onBackPressed() {
        if (!this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        this.mSelectionManager.leaveSelectionMode();
        return true;
    }

    private void updateMainViewPadding() {
        View view = getView();
        if (view != null) {
            view.setPadding(0, 0, 0, 0);
        }
    }

    private static int getTitleId(int fragmentType) {
        switch (fragmentType) {
            case 1:
                return R.plurals.photoshare_down_up_download;
            case 2:
                return R.plurals.photoshare_down_up_download_finish;
            case 3:
                return R.plurals.photoshare_down_up_upload;
            case 4:
                return R.plurals.photoshare_down_up_upload_finish;
            default:
                GalleryLog.w("PhotoShareDownUpFragment", "fragment type is error");
                return 0;
        }
    }

    private static String getTitle(Context context, int fragmentType, int count) {
        int resId = getTitleId(fragmentType);
        return context.getResources().getQuantityString(resId, count, new Object[]{Integer.valueOf(count)});
    }

    private static String getSourcePath(int fragmentType) {
        return "/photoshare/downup/" + fragmentType;
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                enterSelectionMode();
                this.mViewAdapter.notifyDataSetChanged();
                return;
            case 2:
                leaveSelectionMode();
                this.mViewAdapter.notifyDataSetChanged();
                return;
            case 3:
                updateSelectTitle(this.mSelectionMode);
                this.mSelectionMode.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
                this.mViewAdapter.notifyDataSetChanged();
                return;
            case 4:
                updateSelectTitle(this.mSelectionMode);
                this.mSelectionMode.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
                this.mViewAdapter.notifyDataSetChanged();
                return;
            default:
                return;
        }
    }

    public void onSelectionChange(Path path, boolean selected) {
        updateSelectTitle(this.mSelectionMode);
        if (this.mSelectionManager.getSelectedCount() != this.mSelectionManager.getTotalCount()) {
            this.mSelectionMode.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
        }
        this.mViewAdapter.notifyDataSetChanged();
    }

    public void onSelectionLimitExceed() {
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        MediaItem item = this.mDataLoader.get(i);
        if (item == null) {
            return false;
        }
        this.mSelectionManager.setAutoLeaveSelectionMode(false);
        this.mSelectionManager.enterSelectionMode();
        this.mSelectionManager.toggle(item.getPath());
        return true;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (this.mSelectionManager.inSelectionMode()) {
            MediaItem item = this.mDataLoader.get(i);
            if (item != null) {
                this.mSelectionManager.toggle(item.getPath());
            }
        } else if (this.mType == 2 || this.mType == 4) {
            PhotoShareDownUpItem item2 = (PhotoShareDownUpItem) this.mDataLoader.get(i);
            if (item2 != null) {
                String filePath = item2.getFilePath();
                if (!PhotoShareUtils.isFileExists(filePath) || filePath.equals(item2.mFileInfo.getLocalThumbPath()) || filePath.equals(item2.mFileInfo.getLocalBigThumbPath())) {
                    ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_originfile_not_exist, 0);
                } else if (item2.isVideo()) {
                    try {
                        GalleryUtils.playMovieUseHwVPlayer(getActivity(), item2.getUri(), false);
                    } catch (RuntimeException e) {
                        GalleryUtils.playVideoFromCandidate(getActivity(), item2.getUri(), item2.getName(), false);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), SinglePhotoActivity.class);
                    intent.setAction("android.intent.action.VIEW");
                    intent.setDataAndType(item2.getUri(), "image/*");
                    getActivity().startActivity(intent);
                }
            }
        }
    }

    private ProgressDialog createProgressDialog(Context context, int messageId) {
        if (context == null) {
            return null;
        }
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getResources().getString(messageId));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        return dialog;
    }

    private void fileInfoDetailBatchOperation(Action action, ArrayList<FileInfoDetail> fileInfoDetails) {
        if (fileInfoDetails.size() != 0) {
            int group = fileInfoDetails.size() / 100;
            int start = 0;
            for (int i = 0; i <= group; i++) {
                int end = start + 100 > fileInfoDetails.size() ? fileInfoDetails.size() : start + 100;
                FileInfoDetail[] temp = (FileInfoDetail[]) fileInfoDetails.subList(start, end).toArray(new FileInfoDetail[(end - start)]);
                try {
                    switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
                        case 4:
                        case 8:
                            if (1 != this.mType && 2 != this.mType) {
                                if (3 == this.mType || 4 == this.mType) {
                                    PhotoShareUtils.getServer().deleteUploadHistory(temp);
                                    PhotoShareUtils.refreshStatusBar(false);
                                    break;
                                }
                            }
                            PhotoShareUtils.getServer().deleteDownloadHistory(temp);
                            PhotoShareUtils.refreshStatusBar(true);
                            break;
                            break;
                        case 5:
                        case 7:
                            if (1 != this.mType) {
                                if (3 == this.mType) {
                                    PhotoShareUtils.getServer().startUploadTask(temp);
                                    PhotoShareUtils.refreshStatusBar(false);
                                    break;
                                }
                            }
                            PhotoShareUtils.getServer().startDownloadTask(temp);
                            PhotoShareUtils.refreshStatusBar(true);
                            break;
                            break;
                        case 6:
                            if (1 != this.mType) {
                                if (3 == this.mType) {
                                    PhotoShareUtils.getServer().cancelUploadTask(temp);
                                    PhotoShareUtils.refreshStatusBar(false);
                                    break;
                                }
                            }
                            PhotoShareUtils.getServer().cancelDownloadTask(temp);
                            PhotoShareUtils.refreshStatusBar(true);
                            break;
                            break;
                    }
                    PhotoShareUtils.notifyPhotoShareFolderChanged(1);
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
                PhotoShareUtils.updateNotify();
                start = end;
            }
        }
    }
}
