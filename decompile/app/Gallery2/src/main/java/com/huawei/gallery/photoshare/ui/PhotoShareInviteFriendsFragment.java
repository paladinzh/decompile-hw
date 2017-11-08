package com.huawei.gallery.photoshare.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.gallery3d.R;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.AccountInfo;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.photoshare.PhotoShareAddReceiverHandler;
import com.huawei.gallery.photoshare.adapter.PhotoShareReceiverListAdapter;
import com.huawei.gallery.photoshare.ui.PhotoShareReceiverViewGroup.SyncReceiversListener;
import com.huawei.gallery.photoshare.utils.ContactHelper;
import com.huawei.gallery.photoshare.utils.ContactHelper.Contact;
import com.huawei.gallery.photoshare.utils.PhotoShareNoHwAccount;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.DialogTipsWithCheckBox;
import com.huawei.gallery.util.DialogTipsWithCheckBox.Listener;
import com.huawei.gallery.util.PermissionManager;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class PhotoShareInviteFriendsFragment extends PhotoShareBaseShareFragment implements OnEditorActionListener, OnKeyListener, OnItemClickListener, OnTouchListener, OnFocusChangeListener, OnClickListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final Comparator<Contact> mChineseComparator = new Comparator<Contact>() {
        public int compare(Contact o1, Contact o2) {
            Collator myCollator = Collator.getInstance(Locale.CHINA);
            String name1 = TextUtils.isEmpty(o1.getName()) ? o1.getNumber() : o1.getName();
            String name2 = TextUtils.isEmpty(o2.getName()) ? o2.getNumber() : o2.getName();
            if (myCollator.compare(name1, name2) < 0) {
                return -1;
            }
            if (myCollator.compare(name1, name2) > 0) {
                return 1;
            }
            return 0;
        }
    };
    private ArrayList<ShareReceiver> mAllFriends = new ArrayList();
    private DialogInterface.OnClickListener mAllowDataAccessListener;
    private ImageButton mChooseContacts;
    private Listener mDialogTipsListener = new Listener() {
        public void onPositiveButtonClicked(boolean checkBoxSelected) {
            if (checkBoxSelected) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(PhotoShareInviteFriendsFragment.this.getActivity()).edit();
                editor.putBoolean(GallerySettings.KEY_ALLOW_READ_CONTACTS, true);
                editor.apply();
            }
            PhotoShareInviteFriendsFragment.startChooseContactsActivity(PhotoShareInviteFriendsFragment.this.getActivity());
        }

        public void onNegativeButtonClicked(boolean checkBoxSelected) {
        }
    };
    private boolean mIsOKClicked = false;
    private boolean mNeedToShowAddedFriends = false;
    private ArrayList<ShareReceiver> mNewAddedReceiverList = new ArrayList();
    private PhotoShareNoHwAccount mPhotoShareNoHwAccount = null;
    private EditText mReceiverEditor;
    private PhotoShareReceiverViewGroup mReceiverViewGroup;
    private View mRecipientsEditorLayout;
    private ShareInfo mShareInfo;
    private String mSharePath;
    private SyncReceiversListener mSyncReceiversListener = new SyncReceiversListener() {
        public void deleteTotalReceivers(ShareReceiver friend) {
            PhotoShareInviteFriendsFragment.this.mTotalAddedReceiverList.remove(friend);
            PhotoShareInviteFriendsFragment.this.updateTopFriendsAdapter();
        }

        public void deleteNewAddedReceivers(ShareReceiver friend) {
            PhotoShareInviteFriendsFragment.this.mNewAddedReceiverList.remove(friend);
        }

        public void childViewCountChange() {
            int i;
            EditText -get1 = PhotoShareInviteFriendsFragment.this.mReceiverEditor;
            if (PhotoShareInviteFriendsFragment.this.mReceiverViewGroup.getChildCount() > 1) {
                i = 30;
            } else {
                i = 0;
            }
            -get1.setPadding(i, 0, 0, 0);
            if (PhotoShareInviteFriendsFragment.this.mReceiverViewGroup.getChildCount() > 1) {
                PhotoShareInviteFriendsFragment.this.mReceiverEditor.setHint("");
            } else {
                PhotoShareInviteFriendsFragment.this.mReceiverEditor.setHint(R.string.photoshare_share_receiver_default_text);
            }
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence inputTexts, int start, int before, int count) {
            if (!TextUtils.isEmpty(inputTexts) && PhotoShareAddReceiverHandler.endWithSeparator(inputTexts.toString())) {
                PhotoShareInviteFriendsFragment.this.addReceiver(PhotoShareInviteFriendsFragment.this.mReceiverEditor, PhotoShareInviteFriendsFragment.this.mReceiverViewGroup);
            }
            PhotoShareInviteFriendsFragment.this.updateTopFriendsAdapter();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
        }
    };
    private PhotoShareReceiverListAdapter mTopFriendsAdapter;
    private ListView mTopFriendsListView;
    private ArrayList<ShareReceiver> mTotalAddedReceiverList = new ArrayList();

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
            this.mSharePath = data.getString("sharePath");
            this.mTotalAddedReceiverList = data.getParcelableArrayList("totalAddedReceiver");
            this.mNeedToShowAddedFriends = data.getBoolean("needToShowAddedFriends", false);
            if (PhotoShareUtils.isSupportPhotoShare()) {
                if (PhotoShareUtils.getServer() != null) {
                    try {
                        this.mShareInfo = PhotoShareUtils.getServer().getShare(Path.fromString(this.mSharePath).getSuffix());
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                } else {
                    PhotoShareUtils.setRunnable(new Runnable() {
                        public void run() {
                            try {
                                PhotoShareInviteFriendsFragment.this.mShareInfo = PhotoShareUtils.getServer().getShare(Path.fromString(PhotoShareInviteFriendsFragment.this.mSharePath).getSuffix());
                            } catch (RemoteException e) {
                                PhotoShareUtils.dealRemoteException(e);
                            }
                        }
                    });
                }
            }
        }
        this.mPhotoShareNoHwAccount = new PhotoShareNoHwAccount(getActivity());
        this.mAllowDataAccessListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    PhotoShareUtils.startOpenNetService(PhotoShareInviteFriendsFragment.this.getActivity());
                    new Handler(PhotoShareInviteFriendsFragment.this.getActivity().getMainLooper()).postDelayed(new Runnable() {
                        public void run() {
                            PhotoShareInviteFriendsFragment.this.onOKActionClicked();
                        }
                    }, 50);
                } else if (which == -2) {
                    PhotoShareInviteFriendsFragment.this.onOKActionClicked();
                }
            }
        };
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photoshare_invite_friends, container, false);
        this.mChooseContacts = (ImageButton) view.findViewById(R.id.photoshare_contact_icon);
        this.mChooseContacts.setOnClickListener(this);
        initEditor(view);
        return view;
    }

    public void onClick(View view) {
        if (GallerySettings.getBoolean(getActivity(), GallerySettings.KEY_ALLOW_READ_CONTACTS, false)) {
            startChooseContactsActivity(getActivity());
        } else {
            new DialogTipsWithCheckBox(getActivity(), R.string.photoshare_allow_title, R.string.photoshare_read_contacts_and_calllog_content, this.mDialogTipsListener).show();
        }
    }

    public static void startChooseContactsActivity(Activity activity) {
        if (!PermissionManager.requestPermissionsIfNeed(activity, PermissionManager.getPermissionCloudInviteByContacts(), 1002)) {
            try {
                Intent contactIntent = new Intent();
                contactIntent.setAction("android.intent.action.PICK");
                contactIntent.setType("vnd.android.cursor.dir/phone_v2");
                contactIntent.putExtra("com.huawei.community.action.ADD_EMAIL", true);
                contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
                contactIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
                contactIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", 500);
                activity.startActivityForResult(contactIntent, 1);
            } catch (Exception e) {
                GalleryLog.v("PhotoShareInviteFriendsFragment", "startContactActivity error: " + e);
            }
        }
    }

    private void initAllFriends() {
        List allAccounts = null;
        String logOnName = null;
        this.mAllFriends.clear();
        if (PhotoShareUtils.getServer() != null) {
            AccountInfo logOnAccount = PhotoShareUtils.getLogOnAccount();
            try {
                allAccounts = PhotoShareUtils.getServer().getAccountList();
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
            if (logOnAccount != null) {
                logOnName = logOnAccount.getAccountName();
            }
            if (allAccounts != null) {
                for (int i = 0; i < allAccounts.size(); i++) {
                    ShareReceiver receiver = (ShareReceiver) allAccounts.get(i);
                    if (!receiver.getReceiverAcc().equalsIgnoreCase(logOnName)) {
                        receiver.setStatus(0);
                        receiver.setReceiverName(PhotoShareUtils.getValueFromJson(receiver.getShareId(), "receiverName"));
                        this.mAllFriends.add(receiver);
                    }
                }
            }
        }
    }

    private void initEditor(View view) {
        initAllFriends();
        this.mRecipientsEditorLayout = view.findViewById(R.id.recipients_editor_layout);
        this.mReceiverViewGroup = (PhotoShareReceiverViewGroup) view.findViewById(R.id.receiver_viewgroup);
        this.mReceiverEditor = (EditText) view.findViewById(R.id.receiver_editor);
        this.mReceiverEditor.setOnEditorActionListener(this);
        this.mReceiverEditor.setOnKeyListener(this);
        this.mReceiverEditor.setOnFocusChangeListener(this);
        this.mReceiverEditor.addTextChangedListener(this.mTextWatcher);
        this.mReceiverViewGroup.setOnTouchListener(this);
        this.mReceiverViewGroup.setSyncReceiversListener(this.mSyncReceiversListener);
        this.mRecipientsEditorLayout.setOnTouchListener(this);
        this.mTopFriendsListView = (ListView) view.findViewById(R.id.top_friends_list);
        this.mTopFriendsListView.setOnItemClickListener(this);
        this.mTopFriendsAdapter = new PhotoShareReceiverListAdapter(getActivity());
        this.mTopFriendsListView.setAdapter(this.mTopFriendsAdapter);
        this.mTopFriendsAdapter.setData(this.mAllFriends);
        if (this.mNeedToShowAddedFriends && this.mTotalAddedReceiverList.size() > 0) {
            for (int i = 0; i < this.mTotalAddedReceiverList.size(); i++) {
                ShareReceiver info = (ShareReceiver) this.mTotalAddedReceiverList.get(i);
                this.mReceiverViewGroup.addReceiver(info, new PhotoShareReceiverView(getActivity(), this.mReceiverViewGroup));
                this.mNewAddedReceiverList.add(info);
            }
            this.mReceiverEditor.setText("");
            showInputForRecipientEditor(this.mReceiverEditor);
        }
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        ActionMode am = getGalleryActionBar().enterActionMode(false);
        am.setBothAction(Action.NO, Action.OK);
        am.setTitle(getActivity().getResources().getString(R.string.photoshare_invite_member));
        am.show();
    }

    public boolean onBackPressed() {
        this.mReceiverEditor.setOnFocusChangeListener(null);
        return super.onBackPressed();
    }

    public void onActionItemClicked(Action action) {
        GalleryLog.d("PhotoShareInviteFriendsFragment", "id = " + action);
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                PhotoShareUtils.hideSoftInput(this.mReceiverEditor);
                this.mReceiverEditor.setOnFocusChangeListener(null);
                getActivity().finish();
                break;
            case 2:
                if (!this.mIsOKClicked) {
                    if (PhotoShareUtils.isNetworkConnected(getActivity())) {
                        if (PhotoShareUtils.isNetAllowed(getActivity())) {
                            this.mIsOKClicked = true;
                            onOKActionClicked();
                            break;
                        }
                        new Builder(getActivity()).setTitle(R.string.photoshare_allow_title).setMessage(R.string.photoshare_allow_message).setPositiveButton(R.string.photoshare_allow_btn, this.mAllowDataAccessListener).setNegativeButton(R.string.cancel, this.mAllowDataAccessListener).show();
                        return;
                    }
                    ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
                    return;
                }
                return;
        }
    }

    private void onOKActionClicked() {
        PhotoShareUtils.hideSoftInput(this.mReceiverEditor);
        if (this.mReceiverEditor.getText().toString().length() != 0) {
            if (PhotoShareAddReceiverHandler.checkInputValid(getActivity(), this.mReceiverEditor.getText().toString(), this.mReceiverViewGroup, this.mTotalAddedReceiverList)) {
                addReceiver(this.mReceiverEditor, this.mReceiverViewGroup);
            } else {
                this.mReceiverEditor.setText("");
                this.mIsOKClicked = false;
                return;
            }
        }
        showProgressDialog(getActivity().getResources().getQuantityString(R.plurals.photoshare_progress_message_add_receiver, this.mNewAddedReceiverList.size()));
        new Thread() {
            public void run() {
                ArrayList<ShareReceiver> needCheckList = PhotoShareInviteFriendsFragment.this.getNeedCheckAccountInput();
                int group = needCheckList.size() / 100;
                int start = 0;
                for (int i = 0; i <= group; i++) {
                    int end = start + 100 > needCheckList.size() ? needCheckList.size() : start + 100;
                    PhotoShareInviteFriendsFragment.this.checkHwAccount(needCheckList.subList(start, end));
                    start = end;
                }
                PhotoShareInviteFriendsFragment.this.addFriends();
                new Handler(PhotoShareInviteFriendsFragment.this.getActivity().getMainLooper()).post(new Runnable() {
                    public void run() {
                        PhotoShareInviteFriendsFragment.this.dismissProgressDialog();
                        PhotoShareUtils.notifyPhotoShareFolderChanged(1);
                        PhotoShareInviteFriendsFragment.this.getActivity().setResult(-1);
                        PhotoShareInviteFriendsFragment.this.getActivity().finish();
                    }
                });
            }
        }.start();
    }

    private void checkHwAccount(List<ShareReceiver> input) {
        List<ShareReceiver> result = null;
        if (input != null && input.size() != 0) {
            try {
                result = PhotoShareUtils.getServer().isHWAccountList(input);
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
            if (result == null) {
                result = new ArrayList();
            }
            if (result.size() != input.size()) {
                ArrayList<String> notHwList = new ArrayList();
                for (int i = 0; i < input.size(); i++) {
                    ShareReceiver receiver = (ShareReceiver) input.get(i);
                    boolean find = false;
                    for (int j = 0; j < result.size() && !find; j++) {
                        if (((ShareReceiver) result.get(j)).getReceiverAcc().equals(receiver.getReceiverAcc())) {
                            find = true;
                        }
                    }
                    if (!find) {
                        notHwList.add(receiver.getReceiverAcc());
                    }
                }
                doAfterCheckAccount(notHwList);
            }
        }
    }

    private void addFriends() {
        if (this.mShareInfo != null && this.mNewAddedReceiverList.size() != 0) {
            ArrayList<ShareReceiver> needToAdded = getReceiverHasReceiverId();
            if (needToAdded.size() != 0) {
                try {
                    if (PhotoShareUtils.getServer().modifyShareRecAdd(this.mShareInfo, needToAdded) != 0) {
                        insertHwFriendsLocal(needToAdded);
                    } else {
                        this.mPhotoShareNoHwAccount.delete(needToAdded, this.mShareInfo.getOwnerAcc(), this.mShareInfo.getShareId());
                    }
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
            }
        }
    }

    private void insertHwFriendsLocal(ArrayList<ShareReceiver> needToAdded) {
        if (this.mShareInfo != null) {
            List<ShareReceiver> hasAdded = this.mShareInfo.getReceiverList();
            for (ShareReceiver receiver : needToAdded) {
                boolean found = false;
                for (ShareReceiver temp : hasAdded) {
                    if (temp.getReceiverAcc().equalsIgnoreCase(receiver.getReceiverAcc())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    this.mPhotoShareNoHwAccount.insert(this.mShareInfo.getOwnerAcc(), this.mShareInfo.getShareId(), receiver.getReceiverAcc(), receiver.getReceiverName());
                }
            }
        }
    }

    private ArrayList<ShareReceiver> getReceiverHasReceiverId() {
        ArrayList<ShareReceiver> result = new ArrayList();
        if (this.mShareInfo == null) {
            return result;
        }
        try {
            List<ShareReceiver> checked = PhotoShareUtils.getServer().getAccountList();
            for (int i = 0; i < this.mNewAddedReceiverList.size(); i++) {
                ShareReceiver temp = (ShareReceiver) this.mNewAddedReceiverList.get(i);
                for (int j = 0; j < checked.size(); j++) {
                    if (temp.getReceiverAcc().equals(((ShareReceiver) checked.get(j)).getReceiverAcc())) {
                        result.add((ShareReceiver) checked.get(j));
                    }
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        this.mPhotoShareNoHwAccount.delete(result, this.mShareInfo.getOwnerAcc(), this.mShareInfo.getShareId());
        return result;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ShareReceiver info = (ShareReceiver) this.mTopFriendsAdapter.getItem(position);
        if (this.mReceiverViewGroup.exist(info)) {
            ContextedUtils.showToastQuickly(getActivity().getApplicationContext(), MessageFormat.format(getActivity().getString(R.string.photoshare_toast_receiver_exist_Toast), new Object[]{info.getReceiverAcc()}), 0);
            return;
        }
        addReceiver(info, this.mReceiverViewGroup);
        this.mTopFriendsAdapter.removeItem(position);
        this.mReceiverEditor.setText("");
        showInputForRecipientEditor(this.mReceiverEditor);
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.recipients_editor_layout:
            case R.id.receiver_viewgroup:
            case R.id.receiver_editor:
                showInputForRecipientEditor(this.mReceiverEditor);
                updateTopFriendsAdapter();
                break;
        }
        return true;
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == 5 || actionId == 6) {
            if (this.mReceiverEditor.getText().length() == 0) {
                PhotoShareUtils.hideSoftInput(this.mReceiverEditor);
            } else {
                addReceiver(this.mReceiverEditor, this.mReceiverViewGroup);
                updateTopFriendsAdapter();
            }
        }
        return true;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.receiver_editor:
                if (keyCode == 67 && event.getAction() == 0 && this.mReceiverEditor.getSelectionStart() == 0) {
                    int childCount = this.mReceiverViewGroup.getChildCount();
                    if (childCount > 1 && isPort()) {
                        PhotoShareReceiverView recvView = (PhotoShareReceiverView) this.mReceiverViewGroup.getChildAt(childCount - 2);
                        ShareReceiver info = recvView.getFriendsInfo();
                        this.mReceiverViewGroup.deleteReceiver(info, recvView);
                        this.mTotalAddedReceiverList.remove(info);
                        this.mNewAddedReceiverList.remove(info);
                        updateTopFriendsAdapter();
                    }
                    return true;
                }
        }
        return false;
    }

    public void onFocusChange(View v, boolean isFocus) {
        switch (v.getId()) {
            case R.id.receiver_editor:
                if (isFocus) {
                    showInputForRecipientEditor(this.mReceiverEditor);
                } else {
                    addReceiver(this.mReceiverEditor, this.mReceiverViewGroup);
                }
                updateTopFriendsAdapter();
                return;
            default:
                return;
        }
    }

    private boolean isPort() {
        try {
            return getResources().getConfiguration().orientation == 1;
        } catch (Exception e) {
            GalleryLog.d("PhotoShareInviteFriendsFragment", "isPort get orientation error");
            return true;
        }
    }

    private void showInputForRecipientEditor(EditText receiverEditor) {
        if (receiverEditor != null) {
            receiverEditor.requestFocus();
            if (receiverEditor.getText() != null) {
                receiverEditor.setSelection(receiverEditor.getText().length());
            }
            ((InputMethodManager) getActivity().getSystemService("input_method")).showSoftInput(receiverEditor, 0);
        }
    }

    private void updateTopFriendsAdapter() {
        String text = null;
        if (this.mReceiverEditor != null) {
            text = this.mReceiverEditor.getText().toString().trim();
        }
        updateTopFriendsAdapter(text);
    }

    private void updateTopFriendsAdapter(CharSequence inputText) {
        ArrayList<ShareReceiver> friendsExcludeAdded = getFriendsExcludeAdded();
        if (!TextUtils.isEmpty(inputText) && friendsExcludeAdded.size() > 0) {
            ArrayList<ShareReceiver> includeInputTextFriends = new ArrayList();
            for (ShareReceiver info : friendsExcludeAdded) {
                if ((TextUtils.isEmpty(info.getReceiverName()) ? info.getReceiverAcc() : info.getReceiverName()).contains(inputText)) {
                    includeInputTextFriends.add(info);
                }
            }
            friendsExcludeAdded = includeInputTextFriends;
        }
        this.mTopFriendsAdapter.setData(friendsExcludeAdded);
    }

    private ArrayList<ShareReceiver> getFriendsExcludeAdded() {
        ArrayList<ShareReceiver> resultList = new ArrayList();
        resultList.addAll(this.mAllFriends);
        if (this.mAllFriends.size() == 0 || this.mTotalAddedReceiverList.size() == 0) {
            return resultList;
        }
        for (ShareReceiver searchInfo : this.mAllFriends) {
            for (ShareReceiver currentInfo : this.mTotalAddedReceiverList) {
                if (searchInfo.getReceiverAcc().equalsIgnoreCase(currentInfo.getReceiverAcc())) {
                    resultList.remove(searchInfo);
                    break;
                }
            }
        }
        return resultList;
    }

    private ArrayList<ShareReceiver> getNewAddedReceiverList() {
        return this.mNewAddedReceiverList;
    }

    private void addReceiver(EditText receiverEditor, PhotoShareReceiverViewGroup receiverViewGroup) {
        String receiver = receiverEditor.getText().toString();
        if (!TextUtils.isEmpty(receiver)) {
            if (PhotoShareAddReceiverHandler.endWithSeparator(receiver)) {
                if (receiver.length() < 2) {
                    PhotoShareAddReceiverHandler.showInvalidAccountToast(getActivity(), receiver);
                    return;
                }
                receiver = receiver.subSequence(0, receiver.length() - 1).toString();
            }
            receiver = receiver.replace(" ", "");
            if (PhotoShareAddReceiverHandler.checkInputValid(getActivity(), receiver, receiverViewGroup, this.mTotalAddedReceiverList)) {
                addReceiverToViewGroup(receiver, receiverEditor, receiverViewGroup);
            } else {
                receiverEditor.setText("");
            }
        }
    }

    private void addReceiver(ShareReceiver info, PhotoShareReceiverViewGroup receiverViewGroup) {
        if (info != null && receiverViewGroup != null) {
            receiverViewGroup.addReceiver(info, new PhotoShareReceiverView(getActivity(), receiverViewGroup));
            this.mTotalAddedReceiverList.add(info);
            this.mNewAddedReceiverList.add(info);
        }
    }

    private void addReceiverToViewGroup(String receiver, EditText receiverEditor, PhotoShareReceiverViewGroup receiverViewGroup) {
        ShareReceiver info = new ShareReceiver();
        info.setReceiverAcc(receiver);
        receiverViewGroup.addReceiver(info, new PhotoShareReceiverView(getActivity(), receiverViewGroup));
        receiverEditor.setText("");
        this.mTotalAddedReceiverList.add(info);
        this.mNewAddedReceiverList.add(info);
    }

    private ArrayList<ShareReceiver> getNeedCheckAccountInput() {
        ArrayList<ShareReceiver> accountsList = new ArrayList();
        ArrayList<ShareReceiver> list = getNewAddedReceiverList();
        if (list == null || list.size() == 0) {
            return accountsList;
        }
        for (int i = 0; i < list.size(); i++) {
            if (PhotoShareAddReceiverHandler.isNeedCheck((ShareReceiver) list.get(i), this.mAllFriends)) {
                accountsList.add((ShareReceiver) list.get(i));
            }
        }
        return accountsList;
    }

    private void doAfterCheckAccount(ArrayList<String> list) {
        if (list.size() != 0 && this.mShareInfo != null) {
            for (String account : list) {
                Iterator<ShareReceiver> friendsInfoIterator = this.mTotalAddedReceiverList.iterator();
                while (friendsInfoIterator.hasNext()) {
                    ShareReceiver receiver = (ShareReceiver) friendsInfoIterator.next();
                    if (receiver.getReceiverAcc().equalsIgnoreCase(account)) {
                        this.mPhotoShareNoHwAccount.insert(this.mShareInfo.getOwnerAcc(), this.mShareInfo.getShareId(), receiver.getReceiverAcc(), receiver.getReceiverName());
                        friendsInfoIterator.remove();
                    }
                }
                friendsInfoIterator = this.mNewAddedReceiverList.iterator();
                while (friendsInfoIterator.hasNext()) {
                    if (((ShareReceiver) friendsInfoIterator.next()).getReceiverAcc().equalsIgnoreCase(account)) {
                        friendsInfoIterator.remove();
                    }
                }
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        getGalleryActionBar().enterActionMode(false).setBothAction(Action.NONE, Action.NONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == -1 && data != null) {
                    ArrayList<Integer> dataIds = data.getIntegerArrayListExtra("SelItemData_KeyValue");
                    ArrayList<Integer> callsIds = data.getIntegerArrayListExtra("SelItemCalls_KeyValue");
                    if ((dataIds != null && dataIds.size() != 0) || (callsIds != null && callsIds.size() != 0)) {
                        Parcelable[] uris = getUris(dataIds, callsIds);
                        if (uris.length != 0) {
                            List<Contact> result = ContactHelper.getContactInfoForPhoneUris(getActivity(), uris);
                            if (result != null && result.size() != 0) {
                                Collections.sort(result, mChineseComparator);
                                updateReceiverEditor(result);
                                ReportToBigData.report(76);
                                break;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
        }
    }

    private Parcelable[] getUris(ArrayList<Integer> dataIds, ArrayList<Integer> callsIds) {
        int i;
        int callsSize = 0;
        int dataSize = dataIds != null ? dataIds.size() : 0;
        if (callsIds != null) {
            callsSize = callsIds.size();
        }
        Parcelable[] uris = new Parcelable[(dataSize + callsSize)];
        if (dataSize > 0) {
            for (i = 0; i < dataSize; i++) {
                uris[i] = Uri.parse("content://com.android.contacts/data/" + ((Integer) dataIds.get(i)).toString());
            }
        }
        if (callsSize > 0) {
            for (i = 0; i < callsSize; i++) {
                uris[dataSize + i] = Uri.parse("content://call_log/calls/" + ((Integer) callsIds.get(i)).toString());
            }
        }
        return uris;
    }

    private boolean checkReceiverHasAdded(ShareReceiver receiver) {
        for (ShareReceiver shareReceiver : this.mTotalAddedReceiverList) {
            if (shareReceiver.getReceiverAcc().equalsIgnoreCase(receiver.getReceiverAcc())) {
                return true;
            }
        }
        return false;
    }

    private void updateReceiverEditor(List<Contact> result) {
        this.mReceiverEditor.setText("");
        int repeatCount = 0;
        String ownerAccount = null;
        if (this.mShareInfo != null) {
            ownerAccount = this.mShareInfo.getOwnerAcc();
        }
        for (Contact c : result) {
            ShareReceiver info = new ShareReceiver();
            info.setReceiverAcc(c.getNumber());
            info.setReceiverName(c.getName());
            if (checkReceiverHasAdded(info)) {
                repeatCount++;
            } else if (ownerAccount == null || !ownerAccount.equalsIgnoreCase(info.getReceiverAcc())) {
                addReceiver(info, this.mReceiverViewGroup);
            } else {
                ContextedUtils.showToastQuickly(getActivity(), (int) R.string.photoshare_toast_not_allow_to_add_myself_Toast, 0);
            }
        }
        if (repeatCount > 0) {
            ContextedUtils.showToastQuickly(getActivity(), String.format(getResources().getQuantityString(R.plurals.photoshare_friends_exists_Toast, repeatCount), new Object[]{Integer.valueOf(repeatCount)}), 0);
        }
    }
}
