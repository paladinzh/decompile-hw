package com.huawei.gallery.photoshare.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.app.AlbumSetDataLoader;
import com.huawei.gallery.app.ListAlbumPickerActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;

public class PhotoShareCreateNewShareFragment extends PhotoShareBaseShareFragment {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    protected AlbumSetDataLoader mAlbumSetDataLoader = null;
    private OnClickListener mCreateAlbumNetListener;
    private boolean mCreateShareImmediately = false;
    private boolean mIsLoading = false;
    private long mLoadingStart;
    private MediaSet mLocalAlbum;
    private int mLocalAlbumCount = -1;
    private EditText mNewShareEditName;
    private String mShareName;

    private class MyLoadingListener implements LoadingListener {
        private MyLoadingListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            if (PhotoShareCreateNewShareFragment.this.mAlbumSetDataLoader != null) {
                PhotoShareCreateNewShareFragment.this.mIsLoading = false;
                PhotoShareCreateNewShareFragment.this.mLocalAlbumCount = PhotoShareCreateNewShareFragment.this.mAlbumSetDataLoader.size();
                GalleryLog.v("PhotoShareAddNewShareFragment", "count " + PhotoShareCreateNewShareFragment.this.mLocalAlbumCount + " loading cost " + (System.currentTimeMillis() - PhotoShareCreateNewShareFragment.this.mLoadingStart));
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
            iArr[Action.ADD_ALBUM.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 9;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 14;
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
            iArr[Action.NO.ordinal()] = 1;
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
            iArr[Action.OK.ordinal()] = 2;
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
            this.mCreateShareImmediately = data.getBoolean("createNewShare", false);
        }
        DataManager dataManager = getGalleryContext().getDataManager();
        this.mLocalAlbum = dataManager.getMediaSet(Path.fromString(dataManager.getTopSetPath(2097152)));
        this.mAlbumSetDataLoader = new AlbumSetDataLoader(getActivity(), this.mLocalAlbum, 64);
        this.mAlbumSetDataLoader.setLoadingListener(new MyLoadingListener());
        this.mCreateAlbumNetListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(PhotoShareCreateNewShareFragment.this.getActivity());
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            PhotoShareCreateNewShareFragment.this.createShareImmediately();
                        }
                    }, 50);
                }
            }
        };
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photoshare_create_new_share, container, false);
        this.mNewShareEditName = (EditText) view.findViewById(R.id.photoshare_new_share_edittext);
        new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                PhotoShareUtils.showSoftInput(PhotoShareCreateNewShareFragment.this.mNewShareEditName);
            }
        }, 300);
        return view;
    }

    public void onResume() {
        super.onResume();
        if (this.mAlbumSetDataLoader != null) {
            this.mIsLoading = true;
            this.mAlbumSetDataLoader.resume();
            this.mLoadingStart = System.currentTimeMillis();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mAlbumSetDataLoader != null) {
            this.mAlbumSetDataLoader.pause();
        }
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        requestFeature(258);
        ActionMode am = getGalleryActionBar().enterActionMode(false);
        am.setBothAction(Action.NO, Action.OK);
        am.setTitle(getString(R.string.create_share_album_title));
        am.show();
    }

    public void onActionItemClicked(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                PhotoShareUtils.hideSoftInput(this.mNewShareEditName);
                getActivity().finish();
                break;
            case 2:
                if (PhotoShareUtils.isNetworkConnected(getActivity())) {
                    if (this.mNewShareEditName != null) {
                        this.mShareName = this.mNewShareEditName.getText().toString().trim();
                    }
                    if (PhotoShareUtils.isShareNameValid(getActivity().getApplicationContext(), this.mShareName) && PhotoShareUtils.checkCharValid(this.mShareName, getActivity())) {
                        if (PhotoShareUtils.getShareInfo(this.mShareName) == null && !"default-album-1".equals(this.mShareName) && !"default-album-2".equals(this.mShareName)) {
                            if (!this.mCreateShareImmediately) {
                                creatShareMediately();
                                break;
                            }
                            Context context = getActivity();
                            if (!PhotoShareUtils.isNetAllowed(context)) {
                                new Builder(context).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mCreateAlbumNetListener).setNegativeButton(R.string.cancel, this.mCreateAlbumNetListener).show();
                                break;
                            } else {
                                createShareImmediately();
                                break;
                            }
                        }
                        ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_toast_album_already_cloud, 0);
                        return;
                    }
                    return;
                }
                ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
                return;
                break;
        }
    }

    private void creatShareMediately() {
        if (this.mIsLoading || this.mLocalAlbumCount != 0) {
            goToCreateNewShare();
        } else {
            new Thread() {

                /* renamed from: com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$3$1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ Activity val$activity;
                    final /* synthetic */ int val$resultCode;

                    AnonymousClass1(int val$resultCode, Activity val$activity) {
                        this.val$resultCode = val$resultCode;
                        this.val$activity = val$activity;
                    }

                    public void run() {
                        PhotoShareCreateNewShareFragment.this.dismissProgressDialog();
                        if (this.val$resultCode == 0) {
                            PhotoShareCreateNewShareFragment.this.getActivity().setResult(-1, null);
                        } else {
                            ContextedUtils.showToastQuickly(this.val$activity, this.val$activity.getString(R.string.photoshare_toast_create_folder_fail, new Object[]{this.val$activity.getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                        }
                        this.val$activity.finish();
                    }
                }

                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r8 = this;
                    r4 = new com.huawei.android.cg.vo.ShareInfo;
                    r4.<init>();
                    r1 = 1;
                    r5 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r5 = r5.mShareName;	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r4.setShareName(r5);	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r5 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.getServer();	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6 = 0;	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6 = new java.lang.String[r6];	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r1 = r5.createShare(r4, r6);	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r5 = "PhotoShareAddNewShareFragment";	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6.<init>();	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r7 = "createShare result = ";	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6 = r6.append(r7);	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6 = r6.append(r1);	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r6 = r6.toString();	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    com.android.gallery3d.util.GalleryLog.v(r5, r6);	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r3 = r1;
                    r5 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r0 = r5.getActivity();
                    if (r0 == 0) goto L_0x0045;
                L_0x003d:
                    r5 = new com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$3$1;
                    r5.<init>(r1, r0);
                    r0.runOnUiThread(r5);
                L_0x0045:
                    return;
                L_0x0046:
                    r2 = move-exception;
                    com.huawei.gallery.photoshare.utils.PhotoShareUtils.dealRemoteException(r2);	 Catch:{ RemoteException -> 0x0046, all -> 0x005c }
                    r3 = r1;
                    r5 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r0 = r5.getActivity();
                    if (r0 == 0) goto L_0x0045;
                L_0x0053:
                    r5 = new com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$3$1;
                    r5.<init>(r3, r0);
                    r0.runOnUiThread(r5);
                    goto L_0x0045;
                L_0x005c:
                    r5 = move-exception;
                    r3 = r1;
                    r6 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r0 = r6.getActivity();
                    if (r0 == 0) goto L_0x006e;
                L_0x0066:
                    r6 = new com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$3$1;
                    r6.<init>(r3, r0);
                    r0.runOnUiThread(r6);
                L_0x006e:
                    throw r5;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.3.run():void");
                }
            }.start();
        }
    }

    private void createShareImmediately() {
        final ArrayList<String> shareList = PhotoShareUtils.getShareItemList();
        if (shareList.size() != 0) {
            showProgressDialog(getActivity().getString(R.string.photoshare_progress_message_create_new_share));
            new Thread() {

                /* renamed from: com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$4$1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ ArrayList val$fileNeedToAdd;
                    final /* synthetic */ int val$resultCode;

                    AnonymousClass1(int val$resultCode, ArrayList val$fileNeedToAdd) {
                        this.val$resultCode = val$resultCode;
                        this.val$fileNeedToAdd = val$fileNeedToAdd;
                    }

                    public void run() {
                        if (PhotoShareCreateNewShareFragment.this.getActivity() != null) {
                            PhotoShareCreateNewShareFragment.this.dismissProgressDialog();
                            if (this.val$resultCode == 0) {
                                if (this.val$fileNeedToAdd.size() != 0) {
                                    PhotoShareUtils.enableUploadStatusBarNotification(true);
                                    PhotoShareUtils.refreshStatusBar(false);
                                }
                                PhotoShareCreateNewShareFragment.this.getActivity().setResult(-1, null);
                                PhotoShareUtils.goToCreatedShare(PhotoShareCreateNewShareFragment.this.mShareName, PhotoShareCreateNewShareFragment.this.getActivity());
                            } else {
                                ContextedUtils.showToastQuickly(PhotoShareCreateNewShareFragment.this.getActivity().getApplicationContext(), PhotoShareCreateNewShareFragment.this.getActivity().getString(R.string.photoshare_toast_create_folder_fail, new Object[]{PhotoShareCreateNewShareFragment.this.getActivity().getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                            }
                            PhotoShareCreateNewShareFragment.this.getActivity().finish();
                        }
                    }
                }

                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r9 = this;
                    r6 = r0;
                    r2 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.checkMd5ExistsWhenCreateNewShare(r6);
                    r6 = r0;
                    r6 = r6.size();
                    r7 = r2.size();
                    if (r6 <= r7) goto L_0x0020;
                L_0x0012:
                    r6 = r0;
                    r6 = r6.size();
                    r7 = r2.size();
                    r6 = r6 - r7;
                    com.huawei.gallery.photoshare.utils.PhotoShareUtils.showSameFileTips(r6);
                L_0x0020:
                    r6 = r2.size();
                    r5 = new java.lang.String[r6];
                    r4 = new com.huawei.android.cg.vo.ShareInfo;
                    r4.<init>();
                    r0 = 1;
                    r6 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r6 = r6.mShareName;	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r4.setShareName(r6);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r7 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.getServer();	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r6 = r2.toArray(r5);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r6 = (java.lang.String[]) r6;	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r0 = r7.createShare(r4, r6);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r6 = "PhotoShareAddNewShareFragment";	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r7 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r7.<init>();	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r8 = "createShare result = ";	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r7 = r7.append(r0);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r7 = r7.toString();	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    com.android.gallery3d.util.GalleryLog.v(r6, r7);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r3 = r0;
                    r6 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r6 = r6.getActivity();
                    if (r6 == 0) goto L_0x007d;
                L_0x0066:
                    r6 = new android.os.Handler;
                    r7 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r7 = r7.getActivity();
                    r7 = r7.getMainLooper();
                    r6.<init>(r7);
                    r7 = new com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$4$1;
                    r7.<init>(r0, r2);
                    r6.post(r7);
                L_0x007d:
                    return;
                L_0x007e:
                    r1 = move-exception;
                    com.huawei.gallery.photoshare.utils.PhotoShareUtils.dealRemoteException(r1);	 Catch:{ RemoteException -> 0x007e, all -> 0x00a3 }
                    r3 = r0;
                    r6 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r6 = r6.getActivity();
                    if (r6 == 0) goto L_0x007d;
                L_0x008b:
                    r6 = new android.os.Handler;
                    r7 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r7 = r7.getActivity();
                    r7 = r7.getMainLooper();
                    r6.<init>(r7);
                    r7 = new com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$4$1;
                    r7.<init>(r3, r2);
                    r6.post(r7);
                    goto L_0x007d;
                L_0x00a3:
                    r6 = move-exception;
                    r3 = r0;
                    r7 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r7 = r7.getActivity();
                    if (r7 == 0) goto L_0x00c4;
                L_0x00ad:
                    r7 = new android.os.Handler;
                    r8 = com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.this;
                    r8 = r8.getActivity();
                    r8 = r8.getMainLooper();
                    r7.<init>(r8);
                    r8 = new com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment$4$1;
                    r8.<init>(r3, r2);
                    r7.post(r8);
                L_0x00c4:
                    throw r6;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.photoshare.ui.PhotoShareCreateNewShareFragment.4.run():void");
                }
            }.start();
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        getGalleryActionBar().enterActionMode(false).setBothAction(Action.NONE, Action.NONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == -1) {
                    getActivity().finish();
                    PhotoShareUtils.goToCreatedShare(this.mShareName, getActivity());
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void goToCreateNewShare() {
        Intent intent = new Intent(getActivity(), ListAlbumPickerActivity.class).setAction("android.intent.action.GET_CONTENT").setType("*/*");
        intent.putExtra("support-multipick-items", true);
        intent.putExtra("newShareName", this.mShareName);
        getActivity().startActivityForResult(intent, 0);
    }
}
