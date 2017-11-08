package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.gallery3d.R;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SelectionManager.SelectionListener;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.DragListView;
import com.huawei.gallery.util.LayoutHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListAlbumPickerFragment extends AbstractGalleryFragment implements OnItemClickListener, SelectionListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private GalleryActionBar mActionBar;
    protected AlbumSetDataLoader mAlbumSetDataLoader;
    protected DragListView mAlbumSetList;
    protected ListAlbumPickerDataAdapter mAlbumSetListAdapter;
    protected Activity mContext;
    private Bundle mData;
    private DataManager mDataManager;
    private View mEmptyListView;
    protected boolean mGetAlbum;
    protected boolean mGetAlbumIncludeVirtual;
    protected boolean mGetAlbumMultiple;
    private String mGetAlbumPath;
    protected boolean mGetContent;
    private boolean mIsActive = false;
    protected int mMaxSelectCount;
    protected MediaSet mMediaSet;
    protected SelectionManager mSelectionManager;
    protected SelectionMode mSelectionMode;
    private boolean mSupportMultiPickItems = false;

    private class MyLoadingListener implements LoadingListener {
        private MyLoadingListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            if (ListAlbumPickerFragment.this.mIsActive && ListAlbumPickerFragment.this.mAlbumSetDataLoader.size() == 0) {
                GalleryLog.d("ListAlbumPickerFragment", "picker fragment onLoadingFinished and album is empty");
                ListAlbumPickerFragment.this.mAlbumSetList.setEmptyView(ListAlbumPickerFragment.this.mEmptyListView);
                ListAlbumSetFragment.updateEmptyLayoutPadding(ListAlbumPickerFragment.this.mContext, ListAlbumPickerFragment.this.mEmptyListView);
            }
        }

        public void onVisibleRangeLoadFinished() {
        }
    }

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
            iArr[Action.ALL.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 10;
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
            iArr[Action.DEALL.ordinal()] = 2;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 17;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 18;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 19;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 20;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 21;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 22;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 23;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 24;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 25;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 26;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 27;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 28;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 29;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 30;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 31;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 32;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 33;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 34;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 35;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 36;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 3;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 37;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 38;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 4;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 39;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 40;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 41;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 42;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 43;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 44;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 45;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 46;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 47;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 48;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 49;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 50;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 51;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 52;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 53;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 54;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 55;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 57;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 58;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 60;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 61;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 62;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 63;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 64;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 65;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 66;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 67;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 68;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 69;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 70;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 71;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 72;
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
        this.mData = getArguments();
        if (this.mData != null) {
            this.mContext = getActivity();
            this.mDataManager = getGalleryContext().getDataManager();
            this.mSupportMultiPickItems = this.mData.getBoolean("support-multipick-items", false);
            this.mMaxSelectCount = this.mData.getInt("max-select-count", -1);
            this.mGetContent = this.mData.getBoolean("get-content", false);
            this.mGetAlbum = this.mData.getBoolean("get-album", false);
            this.mGetAlbumIncludeVirtual = this.mData.getBoolean("get-album-include-virtual", false);
            this.mGetAlbumMultiple = this.mData.getBoolean("get-album-multiple", false);
            this.mGetAlbumPath = this.mData.getString("choosed_album_path");
            this.mMediaSet = this.mDataManager.getMediaSet(this.mData.getString("media-path"));
            this.mAlbumSetDataLoader = new AlbumSetDataLoader(this.mContext, this.mMediaSet, 64);
            this.mSelectionManager = new SelectionManager(getGalleryContext(), true);
            this.mSelectionManager.setSelectionListener(this);
            this.mSelectionManager.setSourceMediaSet(this.mMediaSet);
            this.mAlbumSetListAdapter = new ListAlbumPickerDataAdapter(this.mContext, this.mAlbumSetDataLoader, this.mSelectionManager, this.mGetAlbum, this.mGetAlbumPath, this.mGetAlbumIncludeVirtual);
            this.mAlbumSetDataLoader.setLoadingListener(new MyLoadingListener());
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflateView = inflater.inflate(R.layout.fragment_list_albumset, container, false);
        this.mAlbumSetList = (DragListView) inflateView.findViewById(R.id.list_albumset);
        this.mAlbumSetListAdapter.setListView(this.mAlbumSetList);
        if (LayoutHelper.isDefaultLandOrientationProduct()) {
            View footerView = inflater.inflate(R.layout.blank_footer_view, this.mAlbumSetList, false);
            this.mAlbumSetList.setFooterDividersEnabled(false);
            this.mAlbumSetList.addFooterView(footerView, null, false);
        }
        this.mEmptyListView = inflateView.findViewById(R.id.list_empty);
        return inflateView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mAlbumSetList.setAdapter(this.mAlbumSetListAdapter);
        this.mAlbumSetList.setOnItemClickListener(this);
    }

    protected void onCreateActionBar(Menu menu) {
        requestFeature(258);
        ActionMode am;
        if (this.mGetContent) {
            am = this.mActionBar.enterActionMode(false);
            am.setBothAction(Action.NO, Action.NONE);
            am.setTitle(this.mData.getInt("get-title", R.string.select_album));
            am.show();
        } else if (!this.mGetAlbum) {
        } else {
            if (this.mGetAlbumMultiple) {
                Set<Path> clickedSet = getSelectedPathsFromPreference();
                if (clickedSet != null) {
                    this.mSelectionManager.setClickedSet(clickedSet);
                }
                this.mSelectionManager.setAutoLeaveSelectionMode(false);
                this.mSelectionManager.enterSelectionMode();
                return;
            }
            am = this.mActionBar.enterActionMode(false);
            am.setBothAction(Action.NO, Action.NONE);
            am.setTitle(this.mData.getInt("get-title", R.string.select_album));
            am.show();
        }
    }

    public void onResume() {
        super.onResume();
        this.mIsActive = true;
        this.mAlbumSetDataLoader.resume();
        this.mAlbumSetListAdapter.resume();
        getGalleryContext().getDataManager().notifyReload(Constant.RELOAD_URI_ALBUMSET, 16390);
    }

    public void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mAlbumSetDataLoader.pause();
        this.mAlbumSetListAdapter.pause();
    }

    public void onActionItemClicked(Action action) {
        GalleryLog.d("ListAlbumPickerFragment", "id = " + action);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                if (this.mSelectionManager.inSelectAllMode()) {
                    this.mSelectionManager.deSelectAll();
                    return;
                } else {
                    this.mSelectionManager.selectAll();
                    return;
                }
            case 3:
                if (this.mSelectionManager.inSelectionMode()) {
                    this.mSelectionManager.leaveSelectionMode();
                }
                this.mContext.finish();
                return;
            case 4:
                if (this.mGetAlbumMultiple) {
                    ArrayList<Path> selectedAlbums = this.mSelectionManager.getSelected(false);
                    ArrayList<String> selectedAlbumsList = new ArrayList();
                    for (Path path : selectedAlbums) {
                        MediaSet mediaSet = this.mDataManager.getMediaSet(path);
                        if ("camera".equalsIgnoreCase(mediaSet.getLabel())) {
                            selectedAlbumsList.add("/local/image/" + MediaSetUtils.getCameraBucketId());
                            selectedAlbumsList.addAll(GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketPathList());
                        } else if ("screenshots".equalsIgnoreCase(mediaSet.getLabel())) {
                            selectedAlbumsList.add("/local/image/" + MediaSetUtils.getScreenshotsBucketID());
                            selectedAlbumsList.addAll(GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketPathList());
                        } else {
                            selectedAlbumsList.add(path.toString());
                        }
                    }
                    saveSelectedPathsToPreference();
                    Intent result = new Intent();
                    result.putStringArrayListExtra("albums-path", selectedAlbumsList);
                    if (selectedAlbums.size() == 1) {
                        result.putExtra("album-name", this.mDataManager.getMediaSet((Path) selectedAlbums.get(0)).getName());
                    }
                    this.mContext.setResult(-1, result);
                    this.mContext.finish();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        onSingleTapUp(position);
    }

    public void onSelectionLimitExceed() {
    }

    public void onSelectionChange(Path path, boolean selected) {
        updateSelectTitile(this.mSelectionMode);
        this.mAlbumSetListAdapter.notifyDataSetChanged();
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                this.mSelectionMode = this.mActionBar.enterSelectionMode(true);
                this.mSelectionMode.setBothAction(Action.NO, Action.OK);
                this.mSelectionMode.setMenu(1, Action.ALL);
                updateSelectTitile(this.mSelectionMode);
                this.mSelectionMode.show();
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                return;
            case 2:
                this.mActionBar.leaveCurrentMode();
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                return;
            case 3:
                updateSelectTitile(this.mSelectionMode);
                this.mSelectionMode.changeAction(Action.ACTION_ID_ALL, Action.ACTION_ID_DEALL);
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                break;
            case 4:
                updateSelectTitile(this.mSelectionMode);
                this.mSelectionMode.changeAction(Action.ACTION_ID_DEALL, Action.ACTION_ID_ALL);
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                break;
            default:
                GalleryLog.d("ListAlbumPickerFragment", "invalid selection mode");
                break;
        }
    }

    protected void onSingleTapUp(int position) {
        MediaSet targetSet = this.mAlbumSetDataLoader.getMediaSet(position);
        if (targetSet == null) {
            GalleryLog.d("ListAlbumPickerFragment", "onSingleTapUp targetSet is null!");
        } else if (this.mSelectionManager.inSelectionMode()) {
            this.mSelectionManager.toggle(targetSet.getPath());
        } else if (this.mGetAlbum && targetSet.isLeafAlbum()) {
            Activity activity = this.mContext;
            Intent result = new Intent();
            if (!this.mGetAlbumIncludeVirtual) {
                result.putExtra("album-path", targetSet.getPath().toString());
            } else if ("camera".equalsIgnoreCase(targetSet.getLabel())) {
                ArrayList<String> galleryStorageCameraBucketPathList = GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketPathList();
                galleryStorageCameraBucketPathList.add(0, "/local/image/" + MediaSetUtils.getCameraBucketId());
                result.putExtra("albums-path", (String[]) galleryStorageCameraBucketPathList.toArray(new String[galleryStorageCameraBucketPathList.size()]));
            } else if ("screenshots".equalsIgnoreCase(targetSet.getLabel())) {
                ArrayList<String> galleryStorageScreenshotsBucketPathList = GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketPathList();
                galleryStorageScreenshotsBucketPathList.add(0, "/local/image/" + MediaSetUtils.getScreenshotsBucketID());
                result.putExtra("albums-path", (String[]) galleryStorageScreenshotsBucketPathList.toArray(new String[galleryStorageScreenshotsBucketPathList.size()]));
            } else {
                result.putExtra("albums-path", new String[]{targetSet.getPath().toString()});
            }
            result.putExtra("album-name", targetSet.getName());
            this.mAlbumSetListAdapter.updateChoosedPath(targetSet.getPath().toString());
            this.mAlbumSetListAdapter.notifyDataSetChanged();
            activity.setResult(-1, result);
            activity.finish();
        } else {
            boolean z;
            Bundle data = this.mData == null ? new Bundle() : new Bundle(this.mData);
            data.putString("media-path", targetSet.getPath().toString());
            data.putBoolean("get-content", this.mGetContent);
            String str = "only-local-camera-video-album";
            if (!targetSet.isVirtual()) {
                z = false;
            } else if ("camera_video".equalsIgnoreCase(targetSet.getLabel())) {
                z = true;
            } else {
                z = "screenshots_video".equalsIgnoreCase(targetSet.getLabel());
            }
            data.putBoolean(str, z);
            data.putInt("max-select-count", this.mMaxSelectCount);
            data.putBoolean("support-multipick-items", this.mSupportMultiPickItems);
            Intent intent = new Intent(this.mContext, SlotAlbumActivity.class);
            intent.putExtras(data);
            if ((this.mContext.getIntent().getFlags() & 8388608) != 0) {
                intent.addFlags(8388608);
            }
            this.mContext.startActivityForResult(intent, 0);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                this.mContext.setResult(resultCode, data);
                if (-1 == resultCode) {
                    this.mContext.finish();
                    return;
                }
                return;
            default:
                return;
        }
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

    private void saveSelectedPathsToPreference() {
        ArrayList<Path> selectedPaths = this.mSelectionManager.getSelected(false);
        Set<String> selectedSet = new HashSet();
        for (Path path : selectedPaths) {
            selectedSet.add(path.toString());
        }
        Editor multiChoosedAlbums = this.mContext.getSharedPreferences("multi_choosed_albums", 0).edit();
        multiChoosedAlbums.putStringSet("multi_choosed_paths", selectedSet);
        multiChoosedAlbums.commit();
    }

    private Set<Path> getSelectedPathsFromPreference() {
        Set<String> selectedSet = this.mContext.getSharedPreferences("multi_choosed_albums", 0).getStringSet("multi_choosed_paths", null);
        Set<Path> clickedSet = new HashSet();
        if (selectedSet == null) {
            return null;
        }
        for (String set : selectedSet) {
            clickedSet.add(Path.fromString(set));
        }
        return clickedSet;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ListAlbumSetFragment.updateEmptyLayoutPadding(this.mContext, this.mEmptyListView);
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mContext.finishActivity(0);
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return true;
    }

    protected void relayoutIfNeed() {
        ListAlbumSetFragment.updateEmptyLayoutPadding(this.mContext, this.mEmptyListView);
    }
}
