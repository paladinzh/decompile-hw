package com.huawei.gallery.photoshare.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.R;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.app.LongTapManager;
import com.android.gallery3d.app.LongTapManager.OnItemClickedListener;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.PhotoShareAlbumSet;
import com.android.gallery3d.data.PhotoShareTimeBucketAlbum;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.DiscoverItemView;
import com.android.gallery3d.ui.DiscoverItemView.Style;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.TabMode;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.app.AbstractGalleryFragment;
import com.huawei.gallery.app.AlbumSetDataLoader;
import com.huawei.gallery.app.ListAlbumPickerActivity;
import com.huawei.gallery.app.PhotoShareAlbumActivity;
import com.huawei.gallery.app.PhotoShareDownloadActivity;
import com.huawei.gallery.app.PhotoShareTimeBucketActivity;
import com.huawei.gallery.app.PhotoShareUploadActivity;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.AlbumSet;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.DiscoverAlbumSet;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.MediaSetListener;
import com.huawei.gallery.photoshare.adapter.PhotoShareMainDataAdapter;
import com.huawei.gallery.photoshare.adapter.PhotoShareMainDataAdapter.AdapterViewOnClickListener;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.story.app.StoryAlbumActivity;
import com.huawei.gallery.story.app.StoryAlbumSetActivity;
import com.huawei.gallery.util.ColorfulUtils;
import java.util.ArrayList;
import java.util.Locale;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareMainFragment extends AbstractGalleryFragment implements OnItemClickListener, OnItemLongClickListener, AdapterViewOnClickListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final /* synthetic */ int[] -com-huawei-gallery-photoshare-DiscoverHeadDataLoader$DiscoverAlbumSetSwitchesValues = null;
    private final Action[] ONLY_MYSHARE_MENU = new Action[]{Action.PHOTOSHARE_CREATE_NEW_SHARE};
    private final Action[] SHARE_SWITCH_CLOSED_MENU = new Action[]{Action.PHOTOSHARE_MANAGE_DOWNLOAD, Action.PHOTOSHARE_SETTINGS};
    private final Action[] SHARE_SWITCH_OPEN_MENU = new Action[]{Action.PHOTOSHARE_CREATE_NEW_SHARE, Action.PHOTOSHARE_MANAGE_UPLOAD, Action.PHOTOSHARE_MANAGE_DOWNLOAD, Action.PHOTOSHARE_SETTINGS};
    private GalleryActionBar mActionBar;
    private AlbumSetDataLoader mAlbumSetDataLoader;
    private PullDownListView mAlbumSetList;
    private PhotoShareMainDataAdapter mAlbumSetListAdapter;
    private ClassifyViewHolder mCategory;
    private AlertDialog mCreateDialog;
    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private boolean mGetContent;
    private Handler mHandler;
    private DiscoverHeadDataLoader mHeadDataLoader = null;
    private boolean mIsActive;
    private boolean mIsOnlyMyShare;
    private View mLoadingTipView;
    private OnItemClickedListener mLongTapItemClickListener = new OnItemClickedListener() {
        public boolean onItemClicked(int resId, int slotIndex) {
            if (PhotoShareMainFragment.this.mTargetMediaSet == null) {
                return true;
            }
            if (resId == R.string.details) {
                PhotoShareMainFragment.this.showDetailWrapper();
                ReportToBigData.reportCloudOperationWithAlbumType(66, PhotoShareMainFragment.this.mTargetMediaSet);
                return true;
            } else if (resId == R.string.photoshare_add_picture) {
                ReportToBigData.report(SmsCheckResult.ESCT_161);
                Intent request = new Intent(PhotoShareMainFragment.this.getActivity(), ListAlbumPickerActivity.class).setAction("android.intent.action.GET_CONTENT").setType("*/*");
                request.putExtra("support-multipick-items", true);
                request.putExtra("max-select-count", 500);
                PhotoShareMainFragment.this.getActivity().startActivityForResult(request, 0);
                return true;
            } else if (resId == R.string.rename) {
                ReportToBigData.report(72);
                PhotoShareMainFragment.this.createDialogIfNeeded(PhotoShareMainFragment.this.mTargetMediaSet.getAlbumInfo().getName(), R.string.rename, PhotoShareMainFragment.this.mReNameDialogButtonListener);
                return true;
            } else if (resId != R.string.photoshare_modify_album_members) {
                return false;
            } else {
                ReportToBigData.reportGotoCloudAlbumMember(false);
                Bundle bundle = new Bundle();
                Intent intent = new Intent();
                if (PhotoShareMainFragment.this.mTargetMediaSet.getAlbumType() == 3) {
                    intent.setClass(PhotoShareMainFragment.this.getActivity(), PhotoShareShowMemberActivity.class);
                    bundle.putString("sharePath", PhotoShareMainFragment.this.mTargetMediaSet.getPath().toString());
                    bundle.putString("shareName", PhotoShareMainFragment.this.mTargetMediaSet.getName());
                } else if (PhotoShareMainFragment.this.mTargetMediaSet.getAlbumType() == 2) {
                    intent.setClass(PhotoShareMainFragment.this.getActivity(), PhotoShareEditFriendsActivity.class);
                    bundle.putString("sharePath", PhotoShareMainFragment.this.mTargetMediaSet.getPath().toString());
                    bundle.putString("shareName", PhotoShareMainFragment.this.mTargetMediaSet.getName());
                } else if (PhotoShareMainFragment.this.mTargetMediaSet.getAlbumType() == 7) {
                    intent.setAction("com.huawei.android.cg.startSnsActivity");
                    bundle.putInt("groupUiType", 2);
                    bundle.putLong("groupId", Long.parseLong(PhotoShareMainFragment.this.mTargetMediaSet.getPath().getSuffix()));
                }
                intent.putExtras(bundle);
                try {
                    PhotoShareMainFragment.this.getActivity().startActivity(intent);
                    ReportToBigData.report(SmsCheckResult.ESCT_146);
                } catch (ActivityNotFoundException e) {
                    GalleryLog.v("PhotoShareMainFragment", "AlbumType " + PhotoShareMainFragment.this.mTargetMediaSet.getAlbumType() + " startActivity Exception");
                }
                return true;
            }
        }
    };
    private LongTapManager mLongTapManager;
    private int mLongTapSlotIndex;
    private MediaSet mMediaSet;
    private ClassifyViewHolder mPeople;
    private ClassifyViewHolder mPlace;
    private ProgressDialog mProgressDialog;
    private OnClickListener mReNameDialogButtonListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    final String fileName = getFileName();
                    if (fileName != null) {
                        PhotoShareMainFragment.this.showProgressDialog(PhotoShareMainFragment.this.getString(R.string.rename));
                        new Thread() {
                            public void run() {
                                final int result = PhotoShareMainFragment.this.mTargetMediaSet.getAlbumInfo().modifyName(fileName);
                                new Handler(PhotoShareMainFragment.this.getActivity().getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        if (1 == result) {
                                            ContextedUtils.showToastQuickly(PhotoShareMainFragment.this.getActivity(), (int) R.string.photoshare_toast_modify_folder_fail_Toast, 0);
                                        } else if (2 == result) {
                                            ContextedUtils.showToastQuickly(PhotoShareMainFragment.this.getActivity(), PhotoShareMainFragment.this.getString(R.string.photoshare_album_toast_modify_folder_fail_Toast, PhotoShareMainFragment.this.getString(R.string.photoshare_toast_fail_common_Toast)), 0);
                                        } else if (7 == result) {
                                            ContextedUtils.showToastQuickly(PhotoShareMainFragment.this.getActivity(), (int) R.string.create_album_file_exist_Toast, 0);
                                        }
                                        PhotoShareMainFragment.this.dismissProgressDialog();
                                    }
                                });
                            }
                        }.start();
                        break;
                    }
                    return;
                default:
                    PhotoShareUtils.hideSoftInput(PhotoShareMainFragment.this.mSetNameTextView);
                    GalleryUtils.setDialogDismissable(dialog, true);
                    GalleryLog.printDFXLog("PhotoShareMainFragment for DFX mCreateDialog " + (PhotoShareMainFragment.this.mCreateDialog == null));
                    if (PhotoShareMainFragment.this.mCreateDialog != null) {
                        GalleryUtils.setDialogDismissable(PhotoShareMainFragment.this.mCreateDialog, true);
                        GalleryUtils.dismissDialogSafely(PhotoShareMainFragment.this.mCreateDialog, null);
                        PhotoShareMainFragment.this.mCreateDialog = null;
                        break;
                    }
                    break;
            }
        }

        private String getFileName() {
            PhotoShareUtils.hideSoftInput(PhotoShareMainFragment.this.mSetNameTextView);
            if (PhotoShareUtils.isNetworkConnected(PhotoShareMainFragment.this.getActivity())) {
                String fileName = PhotoShareMainFragment.this.mSetNameTextView.getText().toString().trim();
                if (!PhotoShareUtils.isShareNameValid(PhotoShareMainFragment.this.getActivity(), fileName) || !PhotoShareUtils.checkCharValid(fileName, PhotoShareMainFragment.this.getActivity())) {
                    PhotoShareMainFragment.this.mSetNameTextView.setFocusable(true);
                    PhotoShareMainFragment.this.mSetNameTextView.setCursorVisible(true);
                    PhotoShareMainFragment.this.mSetNameTextView.requestFocusFromTouch();
                    return null;
                } else if (PhotoShareMainFragment.this.mTargetMediaSet == null) {
                    return null;
                } else {
                    if (!PhotoShareMainFragment.this.mTargetMediaSet.getName().equals(fileName)) {
                        return fileName;
                    }
                    ContextedUtils.showToastQuickly(PhotoShareMainFragment.this.getActivity(), (int) R.string.create_album_file_exist_Toast, 0);
                    return null;
                }
            }
            ContextedUtils.showToastQuickly(PhotoShareMainFragment.this.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
            return null;
        }
    };
    private EditText mSetNameTextView;
    private TextView mSharedAlbumTitle;
    private boolean mShowDetails;
    private ClassifyViewHolder mStory;
    private MediaSet mTargetMediaSet;
    private MyLoadingListener myLoadingListener;

    private static class ClassifyViewHolder implements View.OnClickListener, OnItemClickListener {
        private final AlbumSet albumSet;
        private final ImageView arrow;
        private final DiscoverItemView classifyView;
        private final View labelRoot;
        private final TextView more;
        private final View root;
        private final TextView title;

        private ClassifyViewHolder(View view, AlbumSet albumset, boolean hasMore) {
            this.root = view;
            this.albumSet = albumset;
            this.title = (TextView) view.findViewById(R.id.classify_title);
            this.more = (TextView) view.findViewById(R.id.classify_more);
            this.arrow = (ImageView) view.findViewById(R.id.classify_arrow);
            this.classifyView = (DiscoverItemView) view.findViewById(R.id.classify_content);
            this.labelRoot = view.findViewById(R.id.discover_classify_layout_label_root);
            setItemClickable(albumset);
            setMoreAvaliable(hasMore);
        }

        void setItemClickable(AlbumSet albumSet) {
            OnItemClickListener onItemClickListener;
            if (albumSet.getLoadedDataCount() == 0) {
                onItemClickListener = null;
            } else {
                Object l = this;
            }
            this.classifyView.setOnItemClickListener(onItemClickListener);
        }

        void setMoreAvaliable(boolean enable) {
            if (enable) {
                this.labelRoot.setOnClickListener(this);
                this.more.setVisibility(0);
                this.arrow.setVisibility(0);
                return;
            }
            this.labelRoot.setOnClickListener(null);
            this.more.setVisibility(8);
            this.arrow.setVisibility(8);
        }

        public void setVisible(boolean visible) {
            this.root.setVisibility(visible ? 0 : 8);
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            boolean z = false;
            MediaSet set = this.albumSet.getSubMediaSet(position);
            if (set == null) {
                GalleryLog.v("PhotoShareMainFragment", "PhotoShareMainDataAdapter onItemClick set is null");
                return;
            }
            ReportToBigData.reportForDiscoverAlbumSet(this.albumSet, position);
            GalleryLog.v("PhotoShareMainFragment", "PhotoShareMainDataAdapter Path " + set.getPath());
            Bundle data = new Bundle();
            data.putBoolean("get-content", false);
            data.putString("media-path", set.getPath().toString());
            String str = "local-only";
            if (this.albumSet.entry == DiscoverAlbumSet.PLACE) {
                z = true;
            }
            data.putBoolean(str, z);
            Intent intent = new Intent(view.getContext(), this.albumSet.entry == DiscoverAlbumSet.STORY ? StoryAlbumActivity.class : PhotoShareAlbumActivity.class);
            intent.putExtras(data);
            view.getContext().startActivity(intent);
        }

        public void onClick(View v) {
            boolean z;
            boolean z2 = true;
            Bundle data = new Bundle();
            ReportToBigData.reportForDiscoverAlbumSet(this.albumSet);
            data.putString("media-path", this.albumSet.getMediaSetPath());
            String str = "support-rename";
            if (this.albumSet.entry == DiscoverAlbumSet.PEOPLE) {
                z = true;
            } else {
                z = false;
            }
            data.putBoolean(str, z);
            String str2 = "local-only";
            if (this.albumSet.entry != DiscoverAlbumSet.PLACE) {
                z2 = false;
            }
            data.putBoolean(str2, z2);
            Intent intent = new Intent(v.getContext(), this.albumSet.entry == DiscoverAlbumSet.STORY ? StoryAlbumSetActivity.class : PhotoShareTagAlbumSetActivity.class);
            intent.putExtras(data);
            v.getContext().startActivity(intent);
        }
    }

    private class MyDetailsSource implements DetailsSource {
        private int mIndex;

        private MyDetailsSource() {
        }

        public int setIndex() {
            this.mIndex = PhotoShareMainFragment.this.mLongTapSlotIndex;
            return this.mIndex;
        }

        public MediaDetails getDetails() {
            MediaObject item = PhotoShareMainFragment.this.mAlbumSetDataLoader.getMediaSet(this.mIndex);
            if (item != null) {
                return item.getDetails();
            }
            return null;
        }
    }

    private class MyLoadingListener implements LoadingListener {
        private MyLoadingListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            int i = 8;
            PhotoShareMainFragment.this.mActionBar.disableAnimation(false);
            PhotoShareMainFragment.this.mLoadingTipView.setVisibility(8);
            PhotoShareMainFragment.this.mAlbumSetListAdapter.setCount();
            PhotoShareMainFragment.this.mAlbumSetListAdapter.notifyDataSetChanged();
            boolean isEmpty = PhotoShareMainFragment.this.mAlbumSetListAdapter.getCount() == 0;
            TextView -get9 = PhotoShareMainFragment.this.mSharedAlbumTitle;
            if (!isEmpty) {
                i = 0;
            }
            -get9.setVisibility(i);
        }

        public void onVisibleRangeLoadFinished() {
            PhotoShareMainFragment.this.mLoadingTipView.setVisibility(8);
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 10;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 11;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 12;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 13;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 14;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 15;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 16;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 17;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 18;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 19;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 20;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 21;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 22;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 23;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 24;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 25;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 26;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 27;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 28;
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
            iArr[Action.MOVE.ordinal()] = 36;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 37;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 38;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 39;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 40;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 41;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 42;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 43;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 1;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 44;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 45;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 46;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 47;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 48;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 49;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 50;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 51;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 52;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 53;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 54;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 2;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 55;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 57;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 58;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 59;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 60;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 61;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 3;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 4;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 62;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 63;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 64;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 65;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 66;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 67;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 68;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 5;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 69;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 70;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 71;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 72;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 73;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 74;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 75;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 76;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 77;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 78;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 79;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 80;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 81;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 82;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 83;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 84;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 85;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 86;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 87;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 88;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 89;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 90;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 91;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 92;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 93;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 94;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 95;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 96;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 97;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 98;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 99;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 100;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 101;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 102;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = OfflineMapStatus.EXCEPTION_SDCARD;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 104;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 105;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-photoshare-DiscoverHeadDataLoader$DiscoverAlbumSetSwitchesValues() {
        if (-com-huawei-gallery-photoshare-DiscoverHeadDataLoader$DiscoverAlbumSetSwitchesValues != null) {
            return -com-huawei-gallery-photoshare-DiscoverHeadDataLoader$DiscoverAlbumSetSwitchesValues;
        }
        int[] iArr = new int[DiscoverAlbumSet.values().length];
        try {
            iArr[DiscoverAlbumSet.CATEGORY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DiscoverAlbumSet.PEOPLE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DiscoverAlbumSet.PLACE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DiscoverAlbumSet.STORY.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-gallery-photoshare-DiscoverHeadDataLoader$DiscoverAlbumSetSwitchesValues = iArr;
        return iArr;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        TraceController.beginSection("PhotoShareMainFragment.onCreate");
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data == null) {
            TraceController.endSection();
            return;
        }
        String mediaPath = data.getString("media-path");
        this.mIsOnlyMyShare = mediaPath.contains("myshare");
        this.mMediaSet = getGalleryContext().getDataManager().getMediaSet(mediaPath);
        this.mGetContent = data.getBoolean("get-content", false);
        if (this.mIsOnlyMyShare && !data.getBoolean("inner_share", false)) {
            PhotoShareUtils.storeFilePath(getActivity());
        }
        Boolean needLazyLoad = Boolean.valueOf(data.getBoolean("need-lazy-load"));
        if (needLazyLoad == null || this.mUserHaveFirstLook) {
            GalleryLog.d("PhotoShareMainFragment", "init mUserHaveFirstLook true");
            this.mUserHaveFirstLook = true;
        } else {
            if (!needLazyLoad.booleanValue()) {
                z = true;
            }
            this.mUserHaveFirstLook = z;
            GalleryLog.d("PhotoShareMainFragment", "init mUserHaveFirstLook " + this.mUserHaveFirstLook);
        }
        this.mAlbumSetDataLoader = new AlbumSetDataLoader(getActivity(), this.mMediaSet, 64);
        if (!this.mIsOnlyMyShare) {
            this.mHeadDataLoader = new DiscoverHeadDataLoader(getActivity(), getGalleryContext().getDataManager());
            this.mHeadDataLoader.setDataLoadListener(new MediaSetListener() {
                public void onMediaSetLoad(AlbumSet album) {
                    PhotoShareMainFragment.this.updateDiscoverAlbum(album);
                }

                public void onLoadFinished() {
                    int size = PhotoShareMainFragment.this.mHeadDataLoader.getDataSize();
                    for (int i = 0; i < size; i++) {
                        PhotoShareMainFragment.this.updateDiscoverAlbum(PhotoShareMainFragment.this.mHeadDataLoader.getData(i));
                    }
                }
            });
        }
        this.mAlbumSetListAdapter = new PhotoShareMainDataAdapter(getActivity(), this.mAlbumSetDataLoader);
        this.myLoadingListener = new MyLoadingListener();
        this.mAlbumSetDataLoader.setLoadingListener(this.myLoadingListener);
        this.mDetailsSource = new MyDetailsSource();
        this.mLongTapManager = new LongTapManager((AbstractGalleryActivity) getActivity());
        this.mLongTapManager.setListener(this.mLongTapItemClickListener);
        this.mHandler = new Handler();
        TraceController.endSection();
    }

    private void updateDiscoverAlbum(AlbumSet album) {
        ClassifyViewHolder holder;
        boolean z = true;
        int loadedCount = album.getLoadedDataCount();
        switch (-getcom-huawei-gallery-photoshare-DiscoverHeadDataLoader$DiscoverAlbumSetSwitchesValues()[album.entry.ordinal()]) {
            case 1:
                holder = checkViewHolder(this.mCategory, album, R.id.photoshare_head_category, false);
                this.mCategory = holder;
                break;
            case 2:
                holder = checkViewHolder(this.mPeople, album, R.id.photoshare_head_people, true);
                this.mPeople = holder;
                if (holder != null) {
                    if (loadedCount <= 0) {
                        z = false;
                    }
                    holder.setMoreAvaliable(z);
                    break;
                }
                return;
            case 3:
                holder = checkViewHolder(this.mPlace, album, R.id.photoshare_head_place, true);
                this.mPlace = holder;
                if (holder != null) {
                    if (loadedCount <= 0) {
                        z = false;
                    }
                    holder.setMoreAvaliable(z);
                    break;
                }
                return;
            case 4:
                if (GalleryUtils.IS_STORY_ENABLE) {
                    holder = checkViewHolder(this.mStory, album, R.id.photoshare_head_local_category_story, true);
                    this.mStory = holder;
                    if (holder != null) {
                        holder.setMoreAvaliable(loadedCount > 0);
                        if (loadedCount <= 0) {
                            z = false;
                        }
                        holder.setVisible(z);
                        holder.classifyView.setStyle(Style.Story);
                        break;
                    }
                    return;
                }
                return;
            default:
                throw new RuntimeException("bad album " + album);
        }
        if (holder != null) {
            holder.setItemClickable(album);
            holder.classifyView.setEntry(album);
        }
    }

    private ClassifyViewHolder checkViewHolder(ClassifyViewHolder holder, AlbumSet album, int id, boolean hasMore) {
        if (holder == null) {
            Activity currentActivity = getActivity();
            if (currentActivity == null) {
                GalleryLog.e("PhotoShareMainFragment", "this fragment detach from acitivity");
                return null;
            }
            holder = new ClassifyViewHolder(((ViewStub) currentActivity.findViewById(id)).inflate(), album, hasMore);
            holder.title.setText(getResources().getString(album.getAlbumName()).toUpperCase(Locale.getDefault()));
        }
        return holder;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        TraceController.beginSection("PhotoShareMainFragment.onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
        TraceController.endSection();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TraceController.beginSection("PhotoShareMainFragment.onCreateView");
        View view = inflater.inflate(R.layout.photoshare_main_list, container, false);
        this.mAlbumSetList = (PullDownListView) view.findViewById(R.id.photoshare_list);
        this.mAlbumSetListAdapter.setAdapterView(this.mAlbumSetList);
        this.mAlbumSetListAdapter.setOnClickListener(this);
        this.mLoadingTipView = view.findViewById(R.id.photoshare_main_loading_tips);
        View headView = inflater.inflate(R.layout.discover_list_head, null);
        this.mSharedAlbumTitle = (TextView) headView.findViewById(R.id.title);
        this.mAlbumSetList.addHeaderView(headView);
        setWindowPadding(256, view);
        View footerView = inflater.inflate(R.layout.blank_footer_view, this.mAlbumSetList, false);
        this.mAlbumSetList.setFooterDividersEnabled(false);
        this.mAlbumSetList.addFooterView(footerView, null, false);
        TraceController.endSection();
        return view;
    }

    protected void onCreateActionBar(Menu menu) {
        TraceController.beginSection("PhotoShareMainFragment.onCreateActionBar");
        if (this.mIsOnlyMyShare) {
            requestFeature(256);
            ActionMode am = this.mActionBar.enterActionMode(false);
            am.setBothAction(Action.NO, Action.NONE);
            am.setTitle(getString(R.string.select_album));
            am.setMenu(1, this.ONLY_MYSHARE_MENU);
            am.show();
        } else {
            requestFeature(256);
            TabMode tm = this.mActionBar.enterTabMode(false);
            initFootBarMenu(tm);
            tm.show();
        }
        TraceController.endSection();
    }

    private void initFootBarMenu(ActionBarStateBase actionBarStateBase) {
        if (!PhotoShareUtils.isSupportPhotoShare() || !PhotoShareUtils.isHiCloudLogin()) {
            actionBarStateBase.setMenu(1, Action.PHOTOSHARE_SETTINGS);
        } else if (!PhotoShareUtils.isShareSwitchOpen() || PhotoShareUtils.isCloudNormandyVersion()) {
            actionBarStateBase.setMenu(1, this.SHARE_SWITCH_CLOSED_MENU);
        } else {
            actionBarStateBase.setMenu(2, this.SHARE_SWITCH_OPEN_MENU);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        TraceController.beginSection("PhotoShareMainFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        this.mAlbumSetList.setAdapter(this.mAlbumSetListAdapter);
        TraceController.endSection();
    }

    public void onActionItemClicked(Action action) {
        GalleryLog.d("PhotoShareMainFragment", "id = " + action);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                getActivity().finish();
                return;
            case 2:
                ReportToBigData.report(64);
                Intent intent = new Intent(getActivity(), PhotoShareCreateNewShareActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("createNewShare", this.mIsOnlyMyShare);
                intent.putExtras(bundle);
                if (this.mIsOnlyMyShare) {
                    getActivity().startActivityForResult(intent, 2);
                    return;
                } else {
                    getActivity().startActivity(intent);
                    return;
                }
            case 3:
            case 4:
                Class cls;
                Intent statusIntent = new Intent();
                Context activity = getActivity();
                if (action.ordinal() == Action.ACTION_ID_PHOTOSHARE_MANAGE_DOWNLOAD) {
                    cls = PhotoShareDownloadActivity.class;
                } else {
                    cls = PhotoShareUploadActivity.class;
                }
                statusIntent.setComponent(new ComponentName(activity, cls));
                statusIntent.setAction("com.huawei.gallery.app.photoshare.statusbar.main");
                statusIntent.setFlags(268435456);
                statusIntent.putExtra("key-enter-from", "Menu");
                getActivity().startActivity(statusIntent);
                return;
            case 5:
                getActivity().startActivity(new Intent(getActivity(), GallerySettings.class));
                return;
            default:
                return;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAlbumSetDataLoader.backupData();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        MediaSet mediaSet = this.mAlbumSetDataLoader.getMediaSet(position);
        if (mediaSet != null && !"photoshare_title".equals(mediaSet.getLabel())) {
            this.mTargetMediaSet = mediaSet;
            if (this.mIsOnlyMyShare) {
                PhotoShareUtils.addPhotoToShared(this.mTargetMediaSet.getAlbumInfo().getId());
                getActivity().finish();
                return;
            }
            Bundle data = new Bundle();
            Intent intent = new Intent();
            if (this.mTargetMediaSet.isVirtual()) {
                if (isReallyNoFamilyShare()) {
                    intent.setAction("com.huawei.android.cg.startSnsActivity");
                    data.putInt("groupUiType", 1);
                    ReportToBigData.report(89);
                } else {
                    intent.setClass(getActivity(), PhotoShareCreatingFamilyShareActivity.class);
                }
            } else if (this.mTargetMediaSet instanceof PhotoShareTimeBucketAlbum) {
                data.putString("media-path", this.mTargetMediaSet.getPath().toString());
                data.putBoolean("only-local-camera-video-album", false);
                ReportToBigData.report(SmsCheckResult.ESCT_172);
                intent.setClass(getActivity(), PhotoShareTimeBucketActivity.class);
            } else {
                data.putBoolean("get-content", this.mGetContent);
                data.putString("media-path", this.mTargetMediaSet.getPath().toString());
                intent.setClass(getActivity(), PhotoShareAlbumActivity.class);
            }
            intent.putExtras(data);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                GalleryLog.v("PhotoShareMainFragment", "AlbumType " + this.mTargetMediaSet.getAlbumType() + " startActivity Exception");
            }
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mIsOnlyMyShare) {
            return true;
        }
        MediaSet mediaSet = this.mAlbumSetDataLoader.getMediaSet(position);
        if (mediaSet != null && mediaSet.getLabel() == null) {
            this.mTargetMediaSet = mediaSet;
            this.mLongTapSlotIndex = position;
            this.mLongTapManager.show(this.mTargetMediaSet, position);
        }
        return true;
    }

    public void onResume() {
        TraceController.beginSection("PhotoShareMainFragment.onResume");
        super.onResume();
        this.mIsActive = true;
        if (getUserVisibleHint() && !this.mIsOnlyMyShare) {
            initFootBarMenu(this.mActionBar.getCurrentMode());
            GalleryLog.d("PhotoShareMainFragment", "visible");
        }
        if (this.mHeadDataLoader != null) {
            this.mHeadDataLoader.resume();
        }
        GalleryLog.d("PhotoShareMainFragment", "onResume");
        this.mAlbumSetDataLoader.resume();
        GalleryLog.d("PhotoShareMainFragment", "mUserHaveFirstLook:" + this.mUserHaveFirstLook);
        this.mLoadingTipView.setVisibility(this.mUserHaveFirstLook ? 8 : 0);
        this.mAlbumSetListAdapter.resume();
        TraceController.endSection();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mAlbumSetListAdapter.setCount();
        this.mAlbumSetListAdapter.notifyDataSetChanged();
        setWindowPadding(256);
        super.onConfigurationChanged(newConfig);
    }

    public void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mAlbumSetList.setonRefreshListener(null);
        this.mAlbumSetDataLoader.pause();
        DetailsHelper.pause();
        this.mAlbumSetListAdapter.pause();
        if (this.mHeadDataLoader != null) {
            this.mHeadDataLoader.pause();
        }
    }

    private void showDetailWrapper() {
        Activity activity = getActivity();
        if (this.mAlbumSetDataLoader.size() == 0) {
            ContextedUtils.showToastQuickly(activity.getApplicationContext(), activity.getText(R.string.no_albums_alert_Toast), 0);
        } else if (this.mShowDetails) {
            hideDetails();
        } else {
            showDetails();
        }
    }

    private void showDetails() {
        this.mShowDetails = true;
        if (this.mDetailsHelper == null) {
            this.mDetailsHelper = new DetailsHelper(getGalleryContext(), null, this.mDetailsSource);
            this.mDetailsHelper.setCloseListener(new CloseListener() {
                public void onClose() {
                    PhotoShareMainFragment.this.hideDetails();
                }
            });
        }
        this.mDetailsHelper.show();
    }

    private void hideDetails() {
        this.mShowDetails = false;
        this.mDetailsHelper.hide();
    }

    public boolean onBackPressed() {
        if (this.mShowDetails) {
            hideDetails();
        }
        ReportToBigData.report(37, String.format("{ExitGalleryView:%s}", new Object[]{"FromCloudView"}));
        return false;
    }

    private void createDialogIfNeeded(String defaultName, int titleID, OnClickListener clickListener) {
        if (this.mCreateDialog == null || !this.mCreateDialog.isShowing()) {
            this.mSetNameTextView = new EditText(getActivity());
            this.mSetNameTextView.setSingleLine(true);
            ColorfulUtils.decorateColorfulForEditText(getActivity(), this.mSetNameTextView);
            this.mCreateDialog = GalleryUtils.createDialog(getActivity(), defaultName, titleID, clickListener, null, this.mSetNameTextView);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoShareUtils.showSoftInput(PhotoShareMainFragment.this.mSetNameTextView);
                }
            }, 300);
            return;
        }
        GalleryLog.d("PhotoShareMainFragment", "The dialog is showing, do not create any more");
    }

    private void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(getActivity());
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == -1) {
                    final ArrayList<String> fileList = data.getStringArrayListExtra("select-item-list");
                    if (fileList != null && this.mTargetMediaSet != null) {
                        ReportToBigData.reportAddCloudPicturesWithCount(fileList.size(), this.mTargetMediaSet.getAlbumType() == 7);
                        showProgressDialog(getString(R.string.photoshare_adding_picture));
                        new Thread() {
                            public void run() {
                                ArrayList<String> filePath = PhotoShareUtils.getFilePathFromPathString(PhotoShareMainFragment.this.getGalleryContext(), fileList);
                                ArrayList<String> fileNeedToAdd = PhotoShareUtils.checkMd5ExistsInShare(PhotoShareMainFragment.this.mTargetMediaSet.getAlbumInfo().getId(), filePath);
                                if (filePath.size() > fileNeedToAdd.size()) {
                                    PhotoShareUtils.showFileExitsTips(filePath.size() - fileNeedToAdd.size());
                                }
                                if (!fileNeedToAdd.isEmpty()) {
                                    final int result = PhotoShareMainFragment.this.mTargetMediaSet.getAlbumInfo().addFileToAlbum((String[]) fileNeedToAdd.toArray(new String[fileNeedToAdd.size()]));
                                    GalleryLog.v("PhotoShareMainFragment", "addFileToShare result " + result);
                                    new Handler(PhotoShareMainFragment.this.getActivity().getMainLooper()).post(new Runnable() {
                                        public void run() {
                                            if (result != 0) {
                                                ContextedUtils.showToastQuickly(PhotoShareMainFragment.this.getActivity(), PhotoShareMainFragment.this.getActivity().getString(R.string.photoshare_toast_failed_add_picture, new Object[]{PhotoShareMainFragment.this.getActivity().getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
                                                return;
                                            }
                                            PhotoShareUtils.enableUploadStatusBarNotification(true);
                                            PhotoShareUtils.refreshStatusBar(false);
                                        }
                                    });
                                }
                                new Handler(PhotoShareMainFragment.this.getActivity().getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        PhotoShareMainFragment.this.dismissProgressDialog();
                                    }
                                });
                            }
                        }.start();
                        return;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    private boolean isReallyNoFamilyShare() {
        String[] group = PhotoShareAlbumSet.getFamilyShare();
        if (group == null || group.length == 0) {
            return true;
        }
        return false;
    }

    protected void onUserSelected(boolean selected) {
        super.onUserSelected(selected);
        if (!selected) {
            if (this.mHeadDataLoader != null) {
                this.mHeadDataLoader.freeze();
            }
            if (this.mAlbumSetDataLoader != null) {
                this.mAlbumSetDataLoader.freeze();
            }
        } else if (this.mIsActive) {
            if (this.mHeadDataLoader != null) {
                this.mHeadDataLoader.unfreeze();
            }
            if (this.mAlbumSetDataLoader != null) {
                this.mAlbumSetDataLoader.unfreeze();
            }
        }
    }
}
