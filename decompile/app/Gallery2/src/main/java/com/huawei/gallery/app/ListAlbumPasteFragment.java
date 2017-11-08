package com.huawei.gallery.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Files;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.data.BucketHelper;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.CreateAlbumDialog.CallBackListner;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.DragListView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ListAlbumPasteFragment extends AbstractGalleryFragment implements OnItemClickListener, CallBackListner {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    private GalleryActionBar mActionBar;
    private CreateAlbumDialog mAlbumDialog;
    protected AlbumSetDataLoader mAlbumSetDataLoader;
    protected DragListView mAlbumSetList;
    protected ListAlbumPasteDataAdapter mAlbumSetListAdapter;
    private int mCardLocationType;
    protected Activity mContext;
    private DataManager mDataManager;
    private View mEmptyListView;
    private boolean mIsActive = false;
    private boolean mIsFromScreenshotsAlbum;
    protected MediaSet mMediaSet;
    private int mPasteSourceBucketId;
    private int mPasteType;
    protected int mReloadType = 6;

    private class MyLoadingListener implements LoadingListener {
        private MyLoadingListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            if (ListAlbumPasteFragment.this.mIsActive && ListAlbumPasteFragment.this.mAlbumSetDataLoader.size() == 0) {
                GalleryLog.d("ListAlbumPasteFragment", "paste fragment onLoadingFinished and album is empty");
                ListAlbumPasteFragment.this.mAlbumSetList.setEmptyView(ListAlbumPasteFragment.this.mEmptyListView);
                ListAlbumSetFragment.updateEmptyLayoutPadding(ListAlbumPasteFragment.this.mContext, ListAlbumPasteFragment.this.mEmptyListView);
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
            iArr[Action.ADD.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 5;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 7;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 8;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 9;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 10;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 11;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 12;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 13;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 14;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 16;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 17;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 18;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 22;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 23;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 24;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 25;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 26;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 27;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 28;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 29;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 30;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 31;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 32;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 33;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 34;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 35;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 2;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 36;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 37;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 38;
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
        Bundle data = getArguments();
        if (data != null) {
            this.mContext = getActivity();
            this.mDataManager = getGalleryContext().getDataManager();
            this.mPasteType = data.getInt("key-pastestate", 0);
            this.mCardLocationType = data.getInt("camera-location-type", -1);
            this.mIsFromScreenshotsAlbum = data.getBoolean("is-screenshots-type", false);
            String mediaPath = data.getString("media-path");
            this.mPasteSourceBucketId = data.getInt("key-source-bucketid");
            this.mMediaSet = this.mDataManager.getMediaSet(mediaPath);
            this.mAlbumSetDataLoader = new AlbumSetDataLoader(getActivity(), this.mMediaSet, 64);
            this.mAlbumSetListAdapter = new ListAlbumPasteDataAdapter(getActivity(), this.mAlbumSetDataLoader);
            this.mAlbumDialog = new CreateAlbumDialog(getActivity());
            this.mAlbumDialog.setListner(this);
            initialMergeCardAlbumIfNoExist();
        }
    }

    private void initialMergeCardAlbumIfNoExist() {
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        ArrayList<GalleryStorage> outerGalleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
        if (innerGalleryStorage != null) {
            insertFileForMergeCardAlbum(new File(innerGalleryStorage.getPath(), Constant.CAMERA_PATH));
            if (GalleryUtils.isScreenRecorderExist()) {
                insertFileForMergeCardAlbum(new File(innerGalleryStorage.getPath(), "/Pictures/Screenshots"));
            }
        }
        for (GalleryStorage galleryStorage : outerGalleryStorageList) {
            if (galleryStorage.isMounted()) {
                insertFileForMergeCardAlbum(new File(galleryStorage.getPath(), Constant.CAMERA_PATH));
                if (GalleryUtils.isScreenRecorderExist()) {
                    insertFileForMergeCardAlbum(new File(galleryStorage.getPath(), "/Pictures/Screenshots"));
                }
            }
        }
    }

    private void insertFileForMergeCardAlbum(File filePath) {
        File emptyshowFile = new File(filePath, ".emptyshow");
        try {
            if (!emptyshowFile.exists()) {
                if (emptyshowFile.createNewFile()) {
                    int bucketId = GalleryUtils.getBucketId(filePath.toString());
                    ContentValues values = new ContentValues();
                    values.put("_data", emptyshowFile.toString());
                    values.put("bucket_id", String.valueOf(bucketId));
                    values.put("media_type", String.valueOf(0));
                    values.put("title", ".emptyshow");
                    ((GalleryApp) this.mContext.getApplication()).getContentResolver().insert(EXTERNAL_FILE_URI, values);
                } else {
                    GalleryLog.w("ListAlbumPasteFragment", "cameraFile file create failed ...");
                }
            }
        } catch (IOException e) {
        } catch (Exception e2) {
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_albumset, container, false);
        this.mAlbumSetList = (DragListView) view.findViewById(R.id.list_albumset);
        this.mAlbumSetListAdapter.setListView(this.mAlbumSetList);
        this.mEmptyListView = view.findViewById(R.id.list_empty);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mAlbumSetList.setAdapter(this.mAlbumSetListAdapter);
        this.mAlbumSetList.setOnItemClickListener(this);
        this.mAlbumSetDataLoader.setLoadingListener(new MyLoadingListener());
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        requestFeature(256);
        ActionMode am = this.mActionBar.enterActionMode(false);
        am.setBothAction(Action.NO, Action.NONE);
        am.setTitle(this.mPasteType == 1 ? R.string.copy_to : R.string.cut_to);
        am.setMenu(1, Action.ADD_ALBUM);
        am.show();
    }

    private int getPasteReloadType() {
        int i;
        switch (this.mCardLocationType) {
            case 0:
                if (this.mIsFromScreenshotsAlbum) {
                    i = 4194304;
                } else {
                    i = 262144;
                }
                return 524294 | i;
            case 1:
                if (this.mIsFromScreenshotsAlbum) {
                    i = 2097152;
                } else {
                    i = 131072;
                }
                return 524294 | i;
            case 2:
                if (this.mIsFromScreenshotsAlbum) {
                    i = 1048576;
                } else {
                    i = HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT;
                }
                return 524294 | i;
            default:
                return 524294;
        }
    }

    public void onResume() {
        super.onResume();
        this.mIsActive = true;
        BucketHelper.setPasteSourceBucketId(this.mPasteSourceBucketId);
        this.mReloadType = getPasteReloadType();
        getGalleryContext().getDataManager().notifyReload(Constant.RELOAD_URI_ALBUMSET, this.mReloadType);
        this.mAlbumSetDataLoader.resume();
        this.mAlbumSetListAdapter.resume();
    }

    public void onPause() {
        super.onPause();
        BucketHelper.setPasteSourceBucketId(0);
        this.mIsActive = false;
        this.mAlbumSetDataLoader.pause();
        this.mAlbumSetListAdapter.pause();
    }

    public void onActionItemClicked(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                ListAlbumSetFragment.showNewAlbumDialog(this.mContext, this.mAlbumDialog);
                return;
            case 2:
                getActivity().setResult(0, new Intent());
                getActivity().finish();
                return;
            default:
                return;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        onSingleTapUp(position);
    }

    protected void onSingleTapUp(int position) {
        MediaSet targetSet = this.mAlbumSetDataLoader.getMediaSet(position);
        if (targetSet != null) {
            onPasteDestinationSelect(targetSet.getBucketPath(), targetSet.getName());
        }
    }

    private void onPasteDestinationSelect(String destination, String dirName) {
        if (Constant.DBG) {
            GalleryLog.d("ListAlbumPasteFragment", "Paste destination selected " + destination + ";  Media set Name : " + dirName);
        }
        Bundle data = new Bundle();
        data.putString("key-targetpath", destination);
        data.putString("key-targetfilename", dirName);
        data.putInt("key-pastestate", this.mPasteType);
        data.putBoolean("key-customprogress", true);
        Intent result = new Intent();
        result.putExtras(data);
        getActivity().setResult(-1, result);
        getActivity().finish();
    }

    public void onFinish(boolean created, String dir, String name) {
        if (created && dir != null) {
            onPasteDestinationSelect(dir, name);
        }
    }

    public void dialogDismiss() {
    }

    public boolean onBackPressed() {
        getActivity().setResult(0, new Intent());
        getActivity().finish();
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAlbumDialog.hide();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ListAlbumSetFragment.updateEmptyLayoutPadding(getActivity(), this.mEmptyListView);
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return true;
    }

    protected void relayoutIfNeed() {
        ListAlbumSetFragment.updateEmptyLayoutPadding(this.mContext, this.mEmptyListView);
    }
}
