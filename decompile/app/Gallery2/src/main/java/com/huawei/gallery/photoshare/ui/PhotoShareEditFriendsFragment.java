package com.huawei.gallery.photoshare.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.android.cg.ICloudAlbumService;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.barcode.BarcodeDecoder;
import com.huawei.gallery.photoshare.adapter.PhotoShareAddFriendsAdapter;
import com.huawei.gallery.photoshare.adapter.PhotoShareReceiverStateAdapter;
import com.huawei.gallery.photoshare.ui.FriendSelectionManager.FriendSelectionListener;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment.onDialogButtonClickListener;
import com.huawei.gallery.photoshare.utils.PhotoShareNoHwAccount;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.updateHeadInfoListener;
import com.huawei.gallery.util.ImmersionUtils;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.UIUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoShareEditFriendsFragment extends PhotoShareBaseShareFragment implements updateHeadInfoListener, OnItemClickListener, OnItemLongClickListener, FriendSelectionListener, OnClickListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static long CLICK_MIN_INTERVAL = 300;
    private ImageView mAccountImage;
    private TextView mAccountText;
    private View mAccountView;
    private GalleryActionBar mActionBar;
    private ActionMode mActionMode;
    private BarCodeUpdateListener mBarCodeUpdateListener = new BarCodeUpdateListener();
    private Handler mBarcodeHandler;
    private ImageView mBarcodeImage;
    private View mBarcodeRect;
    private TextView mBarcodeText;
    private HandlerThread mBarcodeThread;
    private ImageView mEmailImage;
    private TextView mEmailText;
    private View mEmailView;
    private boolean mFragmentActive = false;
    private PhotoShareEditFriendsGridview mGroupMember = null;
    private TextView mGroupTitle;
    private Handler mHandler;
    private PhotoShareAddFriendsAdapter mInviteAdapter;
    private ArrayList<ShareReceiver> mInvitedFriendsList = new ArrayList();
    private boolean mIsSeverDone = true;
    private long mLastClickTime = 0;
    private boolean mLastSwicthCondition;
    private ImageView mLinkImage;
    private TextView mLinkText;
    private View mLinkView;
    private ImageView mMessageImage;
    private TextView mMessageText;
    private View mMessageView;
    private boolean mNotNeedUpdateSwitch = false;
    private int mOperationHeight;
    private View mOperationRect;
    private String mOwnerId;
    private PhotoShareNoHwAccount mPhotoShareNoHwAccount = null;
    private TextView mPhotoshareBarcode;
    private PhotoShareReceiverStateAdapter mReceiverAdapter;
    private FriendSelectionManager mSelectionMode = new FriendSelectionManager();
    private TextView mShare;
    private String mShareId;
    private ShareInfo mShareInfo;
    private String mSharePath;
    private Switch mSwitch;
    private String mTitle;

    private class BarCodeUpdateListener implements OnClickListener {
        private BarCodeUpdateListener() {
        }

        public void onClick(View view) {
            PhotoShareEditFriendsFragment.this.showLoading();
            PhotoShareEditFriendsFragment.this.mBarcodeHandler.sendMessage(PhotoShareEditFriendsFragment.this.mBarcodeHandler.obtainMessage(1));
        }
    }

    private class BarcodeHandler extends Handler {
        public BarcodeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    long timeMillis = System.currentTimeMillis();
                    long delayedTime = 0;
                    String uri = PhotoShareEditFriendsFragment.this.checkLink(PhotoShareEditFriendsFragment.this.getBarcodeData());
                    timeMillis = System.currentTimeMillis() - timeMillis;
                    if (uri == null && timeMillis < 1000) {
                        delayedTime = 1000 - timeMillis;
                    }
                    PhotoShareEditFriendsFragment.this.mHandler.sendMessageDelayed(PhotoShareEditFriendsFragment.this.mHandler.obtainMessage(1, uri), delayedTime);
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
            iArr[Action.ADD.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 10;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 11;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 13;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 14;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 15;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 16;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 17;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 18;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 2;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 19;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 20;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 21;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 22;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 23;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 24;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 25;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 26;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 27;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 28;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 29;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 30;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 31;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 32;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 33;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 34;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 35;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 36;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 37;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 38;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 39;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 3;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 40;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 41;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 42;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 4;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 43;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 44;
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
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 5;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 6;
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
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 7;
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

    public void onClick(View view) {
        if (view == null) {
            GalleryLog.d("PhotoShareEditFriendsFragment", "onClick view is null");
            return;
        }
        String inviteType = "{CloudAlbumInviteType:%s}";
        if (view == this.mAccountView) {
            ReportToBigData.report(75, String.format(inviteType, new Object[]{"ByAccount"}));
            addAccount();
        } else if (view == this.mMessageView) {
            ReportToBigData.report(75, String.format(inviteType, new Object[]{"ByMessage"}));
            this.mInviteAdapter.sendMessage();
        } else if (view == this.mEmailView) {
            ReportToBigData.report(75, String.format(inviteType, new Object[]{"ByEmail"}));
            this.mInviteAdapter.sendEmail();
        } else if (view == this.mLinkView) {
            ReportToBigData.report(75, String.format(inviteType, new Object[]{"ByLink"}));
            this.mInviteAdapter.startLink();
        } else {
            GalleryLog.d("PhotoShareEditFriendsFragment", "onClick view is un know");
        }
    }

    public void headInfoChanged(String shareId) {
        if (this.mShareId.equals(shareId)) {
            initReceiverList();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean z = true;
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != 1) {
            z = false;
        }
        updateOperationRectVisible(z);
        updateActionBarMenu();
    }

    private void updateOperationRectVisible(boolean visible) {
        int i = 0;
        if (this.mOperationRect != null) {
            this.mOperationRect.setVisibility(visible ? 0 : 8);
        }
        TextView textView = this.mPhotoshareBarcode;
        if (!visible) {
            i = 4;
        }
        textView.setVisibility(i);
    }

    private void addAccount() {
        if (!this.mSelectionMode.inSelectionMode()) {
            Bundle data = new Bundle();
            data.putString("sharePath", this.mSharePath);
            ArrayList<ShareReceiver> invitedFriendsNotIncludeDeclined = new ArrayList();
            for (ShareReceiver receiver : this.mInvitedFriendsList) {
                if (!((!TextUtils.isEmpty(receiver.getReceiverId()) && receiver.getReceiverId().equals(this.mOwnerId)) || receiver.getStatus() == 2 || receiver.getStatus() == -1)) {
                    invitedFriendsNotIncludeDeclined.add(receiver);
                }
            }
            data.putParcelableArrayList("totalAddedReceiver", invitedFriendsNotIncludeDeclined);
            data.putBoolean("needToShowAddedFriends", false);
            Intent intent = new Intent(getActivity(), PhotoShareInviteFriendsActivity.class);
            intent.putExtras(data);
            getActivity().startActivityForResult(intent, 111);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        this.mSharePath = data.getString("sharePath");
        this.mShareId = Path.fromString(this.mSharePath).getSuffix();
        this.mTitle = data.getString("shareName");
        this.mOperationHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.operation_rect_height);
        this.mSelectionMode.setListener(this);
        this.mReceiverAdapter = new PhotoShareReceiverStateAdapter(getActivity(), this.mSelectionMode);
        this.mInviteAdapter = new PhotoShareAddFriendsAdapter(getActivity());
        this.mPhotoShareNoHwAccount = new PhotoShareNoHwAccount(getActivity());
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                PhotoShareEditFriendsFragment.this.onHandleMessage(msg);
            }
        };
        this.mBarcodeThread = new HandlerThread("Thread for get uri from cloud sdk", 10);
        this.mBarcodeThread.start();
        this.mBarcodeHandler = new BarcodeHandler(this.mBarcodeThread.getLooper());
        this.mFragmentActive = true;
        requestHeadPic();
    }

    private void requestHeadPic() {
        try {
            ShareInfo shareinfo = PhotoShareUtils.getServer().getShare(this.mShareId);
            if (shareinfo != null) {
                ArrayList<String> userIdList = new ArrayList();
                List<ShareReceiver> receivers = shareinfo.getReceiverList();
                userIdList.add(shareinfo.getOwnerId());
                if (receivers != null && receivers.size() > 0) {
                    for (ShareReceiver receiver : receivers) {
                        userIdList.add(receiver.getReceiverId());
                    }
                }
                PhotoShareUtils.getServer().getAlbumHeadPic(this.mShareId, (String[]) userIdList.toArray(new String[userIdList.size()]));
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandler.removeCallbacksAndMessages(null);
        this.mBarcodeHandler.removeCallbacksAndMessages(null);
        this.mBarcodeThread.quit();
        this.mFragmentActive = false;
    }

    public void onResume() {
        super.onResume();
        PhotoShareUtils.setUpdateHeadInfoListener(this);
    }

    public void onPause() {
        super.onPause();
        PhotoShareUtils.setUpdateHeadInfoListener(null);
    }

    private void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (this.mFragmentActive) {
                    String uri = msg.obj;
                    if (uri != null) {
                        hideBarcodeText();
                        this.mBarcodeImage.setImageBitmap(getBarcode(uri));
                        this.mBarcodeImage.invalidate();
                        this.mInviteAdapter.updateUri(uri);
                        updateActionView();
                        updateActionBarMenu();
                        break;
                    }
                    showErrorResult();
                    break;
                }
                return;
            case 2:
                onDeleteShareFriends(msg.arg1);
                break;
            case 3:
                onAddShareFriends(msg.arg1);
                break;
        }
    }

    private void onAddShareFriends(int res) {
        dismissProgressDialog();
        if (res != 0) {
            ContextedUtils.showToastQuickly(getActivity(), String.format(getResources().getQuantityString(R.plurals.photoshare_toast_add_receiver_fail, 1), new Object[]{""}), 0);
            return;
        }
        initReceiverList();
        PhotoShareUtils.notifyPhotoShareFolderChanged(1);
    }

    public void onDeleteShareFriends(int res) {
        dismissProgressDialog();
        if (this.mSelectionMode.inSelectionMode()) {
            this.mSelectionMode.leaveSelectionMode();
        }
        if (res != 0) {
            ContextedUtils.showToastQuickly(getActivity(), getString(R.string.photoshare_toast_delete_receiver_fail, getString(R.string.photoshare_toast_fail_common_Toast)), 0);
            return;
        }
        initReceiverList();
        PhotoShareUtils.notifyPhotoShareFolderChanged(1);
    }

    protected void onCreateActionBar(Menu menu) {
        this.mActionMode = this.mActionBar.enterStandardTitleActionMode(false);
        updateActionBarMenu();
        this.mActionMode.setTitle(this.mTitle);
        this.mActionMode.show();
    }

    private String getBarcodeData() {
        if (this.mShareInfo == null) {
            return null;
        }
        String uri = null;
        try {
            uri = PhotoShareUtils.getServer().createShortLink(this.mShareInfo.getShareId());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        } catch (Exception e2) {
            GalleryLog.i("PhotoShareEditFriendsFragment", "getBarcodeData() failed." + e2.getMessage());
        }
        return uri;
    }

    private String checkLink(String uri) {
        if (uri == null) {
            GalleryLog.d("PhotoShareEditFriendsFragment", "createShortLink null");
            return null;
        } else if (!"1".equals(uri) && !GpsMeasureMode.MODE_2_DIMENSIONAL.equals(uri) && !GpsMeasureMode.MODE_3_DIMENSIONAL.equals(uri) && !"9".equals(uri)) {
            return uri;
        } else {
            GalleryLog.d("PhotoShareEditFriendsFragment", "createShortLink:" + uri);
            return null;
        }
    }

    private Bitmap getBarcode(String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("ENCODE_DATA", uri);
        return BarcodeDecoder.encoder(getActivity(), bundle, "TEXT_TYPE");
    }

    private Rect getOperationRect() {
        int top = LayoutHelper.getStatusBarHeight() + LayoutHelper.getActionBarHeight();
        return new Rect(0, top, LayoutHelper.getScreenWidth(), top + this.mOperationHeight);
    }

    private void initOperationRectBackground() {
        Rect rect = getOperationRect();
        if (rect.width() > 0 && rect.height() > 0) {
            this.mOperationRect.setBackground(UIUtils.getWallpaper(getActivity(), rect, false));
        }
    }

    private void updateClickEnable(boolean enable) {
        if (this.mBarcodeRect != null) {
            if (enable) {
                this.mBarcodeRect.setOnClickListener(this.mBarCodeUpdateListener);
            } else {
                this.mBarcodeRect.setClickable(false);
            }
        }
    }

    private void showLoading() {
        if (this.mBarcodeText != null) {
            this.mBarcodeText.setText(R.string.photoshare_update_barcode);
            this.mBarcodeText.setVisibility(0);
            updateClickEnable(false);
        }
        this.mPhotoshareBarcode.setText(R.string.photoshare_barcode);
    }

    private void showErrorResult() {
        if (this.mBarcodeText != null) {
            this.mBarcodeText.setText(R.string.photoshare_get_barcode_again);
            this.mBarcodeText.setVisibility(0);
            updateClickEnable(true);
        }
        this.mPhotoshareBarcode.setText(R.string.photoshare_get_barcode_result);
    }

    private void hideBarcodeText() {
        if (this.mBarcodeText != null) {
            this.mBarcodeText.setVisibility(8);
            updateClickEnable(false);
        }
        this.mPhotoshareBarcode.setText(R.string.photoshare_barcode);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photoshare_edit_group_main, container, false);
        this.mOperationRect = view.findViewById(R.id.operation_rect);
        this.mSwitch = (Switch) view.findViewById(R.id.switchButton);
        this.mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PhotoShareEditFriendsFragment.this.mLastSwicthCondition = isChecked;
                PhotoShareEditFriendsFragment.this.updateSwitch(isChecked);
            }
        });
        ((ScrollView) view.findViewById(R.id.scroll_view)).setOverScrollMode(2);
        initOperationRectBackground();
        this.mPhotoshareBarcode = (TextView) view.findViewById(R.id.photoshare_barcode);
        this.mGroupMember = (PhotoShareEditFriendsGridview) view.findViewById(R.id.group_member);
        this.mGroupMember.setAdapter(this.mReceiverAdapter);
        this.mGroupMember.setOnItemClickListener(this);
        this.mGroupMember.setOnItemLongClickListener(this);
        this.mGroupMember.updateGridViewColums();
        this.mGroupMember.setFocusable(false);
        Locale defloc = Locale.getDefault();
        this.mGroupTitle = (TextView) view.findViewById(R.id.group_title);
        this.mGroupTitle.setText(this.mGroupTitle.getText().toString().toUpperCase(defloc));
        this.mShare = (TextView) view.findViewById(R.id.share_title);
        this.mShare.setText(this.mShare.getText().toString().toUpperCase(defloc));
        this.mBarcodeRect = view.findViewById(R.id.barcode_rect);
        this.mBarcodeText = (TextView) view.findViewById(R.id.barcode_text);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mBarcodeText, 0);
        this.mBarcodeImage = (ImageView) view.findViewById(R.id.barcode_image);
        updateClickEnable(false);
        this.mAccountView = view.findViewById(R.id.action_account);
        this.mAccountImage = (ImageView) view.findViewById(R.id.action_account_image);
        this.mAccountText = (TextView) view.findViewById(R.id.action_account_text);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mAccountText, 0);
        this.mMessageView = view.findViewById(R.id.action_message);
        this.mMessageImage = (ImageView) view.findViewById(R.id.action_message_image);
        this.mMessageText = (TextView) view.findViewById(R.id.action_message_text);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mMessageText, 0);
        this.mEmailView = view.findViewById(R.id.action_email);
        this.mEmailImage = (ImageView) view.findViewById(R.id.action_email_image);
        this.mEmailText = (TextView) view.findViewById(R.id.action_email_text);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mEmailText, 0);
        this.mLinkView = view.findViewById(R.id.action_link);
        this.mLinkImage = (ImageView) view.findViewById(R.id.action_link_image);
        this.mLinkText = (TextView) view.findViewById(R.id.action_link_text);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mLinkText, 0);
        updateActionView();
        if (PhotoShareUtils.isSupportPhotoShare()) {
            if (PhotoShareUtils.getServer() != null) {
                initReceiverList();
                initSwitch();
            } else {
                PhotoShareUtils.setRunnable(new Runnable() {
                    public void run() {
                        PhotoShareEditFriendsFragment.this.initReceiverList();
                        PhotoShareEditFriendsFragment.this.initSwitch();
                    }
                });
            }
        }
        if (this.mTitle != null) {
            this.mInviteAdapter.setUriTitle(getString(R.string.photoshare_uri_title, this.mTitle));
        }
        updateOperationRectVisible(isPort());
        this.mBarcodeHandler.sendMessage(this.mBarcodeHandler.obtainMessage(1));
        return view;
    }

    public void onActionItemClicked(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                getActivity().finish();
                return;
            case 2:
                deleteFriends();
                return;
            case 3:
                if (this.mSelectionMode.inSelectionMode()) {
                    this.mSelectionMode.leaveSelectionMode();
                    return;
                }
                return;
            case 4:
                addAccount();
                return;
            case 5:
                this.mInviteAdapter.sendEmail();
                return;
            case 6:
                this.mInviteAdapter.startLink();
                return;
            case 7:
                this.mInviteAdapter.sendMessage();
                return;
            default:
                return;
        }
    }

    private boolean isPort() {
        GalleryLog.printDFXLog("isPort function for DFX");
        try {
            return getResources().getConfiguration().orientation == 1;
        } catch (Exception e) {
            GalleryLog.d("PhotoShareEditFriendsFragment", "isPort get orientation error");
            return true;
        }
    }

    private void updateActionView() {
        ImmersionUtils.setImageViewSrcImmersionStyle(this.mAccountImage, Action.PHOTOSHARE_ACCOUNT.iconResID, Action.PHOTOSHARE_ACCOUNT.iconWhiteResID, 0);
        if (this.mSelectionMode.inSelectionMode()) {
            this.mAccountView.setClickable(false);
            this.mAccountImage.setImageAlpha(77);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mAccountText, R.color.photo_share_edit_friends_text_disable_color_light, R.color.photo_share_edit_friends_text_disable_color);
        } else {
            this.mAccountView.setOnClickListener(this);
            this.mAccountImage.setImageAlpha(255);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mAccountText, R.color.photo_share_edit_friends_text_enable_color_light, R.color.photo_share_edit_friends_text_enable_color);
        }
        ImmersionUtils.setImageViewSrcImmersionStyle(this.mEmailImage, Action.PHOTOSHARE_EMAIL.iconResID, Action.PHOTOSHARE_EMAIL.iconWhiteResID, 0);
        if (this.mSelectionMode.inSelectionMode() || !this.mInviteAdapter.isEmailEnable()) {
            this.mEmailView.setClickable(false);
            this.mEmailImage.setImageAlpha(77);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mEmailText, R.color.photo_share_edit_friends_text_disable_color_light, R.color.photo_share_edit_friends_text_disable_color);
        } else {
            this.mEmailView.setOnClickListener(this);
            this.mEmailImage.setImageAlpha(255);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mEmailText, R.color.photo_share_edit_friends_text_enable_color_light, R.color.photo_share_edit_friends_text_enable_color);
        }
        ImmersionUtils.setImageViewSrcImmersionStyle(this.mMessageImage, Action.PHOTOSHARE_MESSAGE.iconResID, Action.PHOTOSHARE_MESSAGE.iconWhiteResID, 0);
        if (this.mSelectionMode.inSelectionMode() || !this.mInviteAdapter.isMessageEnable()) {
            this.mMessageView.setClickable(false);
            this.mMessageImage.setImageAlpha(77);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mMessageText, R.color.photo_share_edit_friends_text_disable_color_light, R.color.photo_share_edit_friends_text_disable_color);
        } else {
            this.mMessageView.setOnClickListener(this);
            this.mMessageImage.setImageAlpha(255);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mMessageText, R.color.photo_share_edit_friends_text_enable_color_light, R.color.photo_share_edit_friends_text_enable_color);
        }
        ImmersionUtils.setImageViewSrcImmersionStyle(this.mLinkImage, Action.PHOTOSHARE_LINK.iconResID, Action.PHOTOSHARE_LINK.iconWhiteResID, 0);
        if (this.mSelectionMode.inSelectionMode() || !this.mInviteAdapter.isLinkEnable()) {
            this.mLinkView.setClickable(false);
            this.mLinkImage.setImageAlpha(77);
            ImmersionUtils.setTextViewColorImmersionStyle(this.mLinkText, R.color.photo_share_edit_friends_text_disable_color_light, R.color.photo_share_edit_friends_text_disable_color);
            return;
        }
        this.mLinkView.setOnClickListener(this);
        this.mLinkImage.setImageAlpha(255);
        ImmersionUtils.setTextViewColorImmersionStyle(this.mLinkText, R.color.photo_share_edit_friends_text_enable_color_light, R.color.photo_share_edit_friends_text_enable_color);
    }

    private void updateActionBarMenu() {
        if (this.mActionMode != null) {
            try {
                if (this.mSelectionMode.inSelectionMode()) {
                    this.mActionMode.setMenu(1, Action.DEL);
                    setMenuVisibility(true);
                    requestFeature(256);
                    return;
                }
                ArrayList<Action> actionList = new ArrayList();
                actionList.add(Action.PHOTOSHARE_ACCOUNT);
                if (this.mInviteAdapter.isMessageEnable()) {
                    actionList.add(Action.PHOTOSHARE_MESSAGE);
                }
                if (this.mInviteAdapter.isEmailEnable()) {
                    actionList.add(Action.PHOTOSHARE_EMAIL);
                }
                if (this.mInviteAdapter.isLinkEnable()) {
                    actionList.add(Action.PHOTOSHARE_LINK);
                }
                this.mActionMode.setMenu(actionList.size(), (Action[]) actionList.toArray(new Action[actionList.size()]));
                if (isPort()) {
                    requestFeature(258);
                    this.mActionBar.setMenuVisible(false);
                } else {
                    requestFeature(256);
                    this.mActionBar.setMenuVisible(true);
                }
            } catch (IllegalStateException e) {
                GalleryLog.d("PhotoShareEditFriendsFragment", "updateActionBarMenu error");
            }
        }
    }

    private void deleteFriends() {
        if (this.mShareInfo != null) {
            PhotoShareAlertDialogFragment dialog = PhotoShareAlertDialogFragment.newInstance(getString(R.string.photoshare_delete_title_receiver), getString(R.string.photoshare_delete_title_receiver_desc), getString(R.string.dialog_gallerycloud_removefriend), true);
            dialog.setOnDialogButtonClickListener(new onDialogButtonClickListener() {
                public void onPositiveClick() {
                    PhotoShareEditFriendsFragment.this.showProgressDialog(PhotoShareEditFriendsFragment.this.getString(R.string.photoshare_progress_message_delete_receiver));
                    new Thread() {
                        public void run() {
                            List<ShareReceiver> delList = new ArrayList();
                            ArrayList<ShareReceiver> localList = new ArrayList();
                            for (ShareReceiver receiver : PhotoShareEditFriendsFragment.this.mSelectionMode.getAllItems()) {
                                if (receiver.getStatus() == -1) {
                                    localList.add(receiver);
                                } else {
                                    delList.add(receiver);
                                }
                            }
                            PhotoShareEditFriendsFragment.this.mPhotoShareNoHwAccount.delete(localList, PhotoShareEditFriendsFragment.this.mShareInfo.getOwnerAcc(), PhotoShareEditFriendsFragment.this.mShareInfo.getShareId());
                            int result = 0;
                            if (delList.size() > 0) {
                                try {
                                    result = PhotoShareUtils.getServer().modifyShareRecDel(PhotoShareEditFriendsFragment.this.mShareInfo, delList);
                                } catch (RemoteException e) {
                                    PhotoShareUtils.dealRemoteException(e);
                                }
                            }
                            PhotoShareEditFriendsFragment.this.mHandler.sendMessage(PhotoShareEditFriendsFragment.this.mHandler.obtainMessage(2, result, 0));
                        }
                    }.start();
                }
            });
            dialog.show(getActivity().getSupportFragmentManager(), "");
        }
    }

    private void initReceiverList() {
        try {
            this.mShareInfo = PhotoShareUtils.getServer().getShare(this.mShareId);
            if (this.mShareInfo != null) {
                this.mOwnerId = this.mShareInfo.getOwnerId();
                ArrayList<ShareReceiver> list = new ArrayList();
                Object obj = null;
                List<ShareReceiver> tempList = PhotoShareUtils.getServer().getAlbumLocalHeadPic(this.mShareId);
                if (tempList != null && tempList.size() > 0) {
                    for (ShareReceiver receiver : tempList) {
                        if (receiver.getReceiverId().equals(this.mOwnerId)) {
                            obj = receiver;
                            tempList.remove(receiver);
                            break;
                        }
                    }
                    list.addAll(tempList);
                }
                if (obj == null) {
                    obj = new ShareReceiver();
                    obj.setReceiverId(this.mOwnerId);
                }
                list.add(0, obj);
                this.mInvitedFriendsList.clear();
                this.mInvitedFriendsList.addAll(list);
                if (!(this.mReceiverAdapter == null || this.mShareInfo == null)) {
                    this.mInvitedFriendsList.addAll(this.mPhotoShareNoHwAccount.query(this.mShareInfo.getOwnerAcc(), this.mShareInfo.getShareId()));
                    this.mReceiverAdapter.setData(this.mInvitedFriendsList);
                    this.mReceiverAdapter.notifyDataSetChanged();
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 111:
                if (resultCode == -1) {
                    initReceiverList();
                    requestHeadPic();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private ShareReceiver getCurrentItem(int index) {
        if (index == 0 || index >= this.mInvitedFriendsList.size()) {
            return null;
        }
        return (ShareReceiver) this.mInvitedFriendsList.get(index);
    }

    private void showInvitedAgain(final ShareReceiver item) {
        if (this.mShareInfo != null) {
            String account = TextUtils.isEmpty(item.getReceiverName()) ? item.getReceiverAcc() : item.getReceiverName();
            String message = getString(R.string.photoshare_invite_friend_btn);
            if (account != null) {
                message = getString(R.string.photoshare_reinvite_member, account);
            }
            PhotoShareAlertDialogFragment dialog = PhotoShareAlertDialogFragment.newInstance(null, message, getString(R.string.photoshare_invite_friend_btn), false);
            dialog.setOnDialogButtonClickListener(new onDialogButtonClickListener() {
                public void onPositiveClick() {
                    PhotoShareEditFriendsFragment.this.showProgressDialog(PhotoShareEditFriendsFragment.this.getActivity().getResources().getQuantityString(R.plurals.photoshare_progress_message_add_receiver, 1));
                    final ShareReceiver shareReceiver = item;
                    new Thread() {
                        public void run() {
                            int result = -1;
                            ArrayList<ShareReceiver> addList = new ArrayList();
                            addList.add(shareReceiver);
                            try {
                                if (shareReceiver.getStatus() == -1) {
                                    List<ShareReceiver> hwAccount = PhotoShareUtils.getServer().isHWAccountList(addList);
                                    if (hwAccount != null && hwAccount.size() > 0) {
                                        List<ShareReceiver> temp = PhotoShareUtils.getServer().getAccountList();
                                        addList.clear();
                                        for (ShareReceiver receiver : temp) {
                                            if (receiver.getReceiverAcc().equalsIgnoreCase(((ShareReceiver) hwAccount.get(0)).getReceiverAcc())) {
                                                addList.add(receiver);
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (addList.size() > 0) {
                                    result = PhotoShareUtils.getServer().modifyShareRecAdd(PhotoShareEditFriendsFragment.this.mShareInfo, addList);
                                }
                            } catch (RemoteException e) {
                                PhotoShareUtils.dealRemoteException(e);
                            }
                            if (result == 0 && shareReceiver.getStatus() == -1) {
                                PhotoShareEditFriendsFragment.this.mPhotoShareNoHwAccount.delete(addList, PhotoShareEditFriendsFragment.this.mShareInfo.getOwnerAcc(), PhotoShareEditFriendsFragment.this.mShareInfo.getShareId());
                            }
                            PhotoShareEditFriendsFragment.this.mHandler.sendMessage(PhotoShareEditFriendsFragment.this.mHandler.obtainMessage(3, result, 0));
                        }
                    }.start();
                }
            });
            dialog.show(getFragmentManager(), "");
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ShareReceiver currentItem = getCurrentItem(i);
        if (currentItem == null) {
            GalleryLog.d("PhotoShareEditFriendsFragment", "onItemClick index:" + i + " currentItem is null");
            return;
        }
        if (this.mSelectionMode.inSelectionMode()) {
            this.mSelectionMode.toggle(currentItem);
        } else {
            long now = System.currentTimeMillis();
            long interval = now - this.mLastClickTime;
            if (interval >= CLICK_MIN_INTERVAL || interval <= 0) {
                this.mLastClickTime = now;
                if (2 == currentItem.getStatus() || -1 == currentItem.getStatus()) {
                    showInvitedAgain(currentItem);
                }
            }
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (this.mSelectionMode.inSelectionMode()) {
            return false;
        }
        ShareReceiver currentItem = getCurrentItem(i);
        if (currentItem == null) {
            return false;
        }
        this.mSelectionMode.enterSelectionMode();
        this.mSelectionMode.toggle(currentItem);
        return true;
    }

    private void leaveActionbarSelectionMode() {
        this.mActionMode = this.mActionBar.enterStandardTitleActionMode(false);
        updateActionBarMenu();
        this.mActionMode.setTitle(this.mTitle);
        this.mActionMode.show();
    }

    private void enterActionbarSelectionMode() {
        SelectionMode sm = this.mActionBar.enterSelectionMode(true);
        sm.setLeftAction(Action.NO);
        sm.setTitle((int) R.string.has_selected);
        sm.setRightAction(Action.NONE);
        sm.show();
        updateActionBarMenu();
    }

    private void updateSelectTitle(SelectionMode mode) {
        if (mode != null) {
            int count = this.mSelectionMode.size();
            if (count == 0) {
                mode.setTitle(getResources().getString(R.string.no_selected));
                mode.setActionEnable(false, Action.ACTION_ID_DEL);
                mode.setCount(null);
                return;
            }
            mode.setTitle(getResources().getString(R.string.has_selected));
            mode.setCount(count);
            mode.setActionEnable(true, Action.ACTION_ID_DEL);
        }
    }

    public boolean onBackPressed() {
        if (!this.mSelectionMode.inSelectionMode()) {
            return false;
        }
        this.mSelectionMode.leaveSelectionMode();
        return true;
    }

    public void onModeChange(int mode) {
        if (mode == 1) {
            enterActionbarSelectionMode();
        } else {
            leaveActionbarSelectionMode();
            this.mReceiverAdapter.notifyDataSetChanged();
        }
        updateActionView();
    }

    public void onItemChange(ShareReceiver item, boolean selected) {
        updateSelectTitle((SelectionMode) this.mActionBar.getCurrentMode());
        this.mReceiverAdapter.notifyDataSetChanged();
    }

    private void updateSwitch(final boolean isChecked) {
        if (PhotoShareUtils.getServer() != null) {
            if (this.mNotNeedUpdateSwitch) {
                this.mNotNeedUpdateSwitch = false;
                return;
            }
            if (this.mIsSeverDone) {
                this.mIsSeverDone = false;
                new Thread() {
                    public void run() {
                        int i = 0;
                        try {
                            ICloudAlbumService server = PhotoShareUtils.getServer();
                            String -get4 = PhotoShareEditFriendsFragment.this.mShareId;
                            if (!isChecked) {
                                i = 1;
                            }
                            int result = server.updateShareInfoPrivilege(-get4, i);
                            ShareInfo tempShareInfo = PhotoShareUtils.getServer().getShare(PhotoShareEditFriendsFragment.this.mShareId);
                            if (tempShareInfo != null) {
                                PhotoShareEditFriendsFragment.this.mShareInfo = tempShareInfo;
                            }
                            if (result != 0) {
                                Activity activity = PhotoShareEditFriendsFragment.this.getActivity();
                                if (activity != null) {
                                    final boolean z = isChecked;
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            boolean z;
                                            boolean z2 = false;
                                            PhotoShareEditFriendsFragment.this.mNotNeedUpdateSwitch = true;
                                            Switch -get6 = PhotoShareEditFriendsFragment.this.mSwitch;
                                            if (z) {
                                                z = false;
                                            } else {
                                                z = true;
                                            }
                                            -get6.setChecked(z);
                                            PhotoShareEditFriendsFragment photoShareEditFriendsFragment = PhotoShareEditFriendsFragment.this;
                                            if (!z) {
                                                z2 = true;
                                            }
                                            photoShareEditFriendsFragment.mLastSwicthCondition = z2;
                                        }
                                    });
                                }
                            } else {
                                String str = "{Status:%s}";
                                Object[] objArr = new Object[1];
                                objArr[0] = isChecked ? "On" : "Off";
                                ReportToBigData.report(93, String.format(str, objArr));
                            }
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                        }
                        PhotoShareEditFriendsFragment.this.mIsSeverDone = true;
                        PhotoShareEditFriendsFragment.this.checkSwitch(isChecked);
                    }
                }.start();
            }
        }
    }

    private void checkSwitch(boolean isChecked) {
        if (isChecked != this.mLastSwicthCondition) {
            updateSwitch(this.mLastSwicthCondition);
        }
    }

    private void initSwitch() {
        if (this.mShareInfo == null) {
            this.mSwitch.setChecked(false);
        } else if (this.mShareInfo.getLocalThumbPath() != null && this.mShareInfo.getLocalThumbPath().size() != 0) {
            boolean isChecked = "0".equalsIgnoreCase((String) this.mShareInfo.getLocalThumbPath().get(0));
            if (isChecked) {
                this.mNotNeedUpdateSwitch = true;
            }
            this.mSwitch.setChecked(isChecked);
        } else {
            return;
        }
        this.mSwitch.setVisibility(0);
    }
}
