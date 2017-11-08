package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.mms.attachment.datamodel.media.GalleryCursorManager;
import com.android.mms.attachment.datamodel.media.GalleryCursorManager.GalleryCursorListener;
import com.android.mms.attachment.datamodel.media.RichMessageManager;
import com.android.mms.attachment.ui.mediapicker.GalleryCompressAdapter;
import com.android.mms.ui.RichMessageEditor.RichAttachmentListener;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor.RcsGroupRichAttachmentListener;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcseMmsExt;
import com.huawei.rcs.utils.RcseMmsExt.SendModeSetListener;
import java.io.File;

public class GalleryCompressFragment extends HwBaseFragment implements RichAttachmentListener, RcsGroupRichAttachmentListener {
    private int mActivityId;
    private int mBarHeightPort;
    private ImageView mBtnBackView;
    private int mCurrentPage;
    private TextView mCurrentPageTextView = null;
    private View mFileInfoView;
    private RadioButton mFullSizeRadioButton;
    private GalleryCompressAdapter mGalleryCompressAdapter;
    private ViewPager mGalleryCompressViewPager = null;
    private GalleryCursorListener mGalleryCursorListener = new GalleryCursorListener() {
        public void refreshCursor() {
            Cursor cursor = GalleryCursorManager.get().getGalleryCursor();
            if (cursor == null || cursor.isClosed()) {
                GalleryCompressFragment.this.getActivity().finish();
                return;
            }
            GalleryCompressFragment.this.mTotalPages = cursor.getCount() - 1;
            if (GalleryCompressFragment.this.mGalleryCompressAdapter != null) {
                GalleryCompressFragment.this.mGalleryCompressAdapter.refreshGalleryCursor(cursor);
            }
            GalleryCompressFragment.this.refreshViewPager(true);
        }
    };
    private Handler mHanderEx = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10001:
                case 10003:
                    GalleryCompressFragment.this.updateVeiwStatus();
                    return;
                case 10002:
                    GalleryCompressFragment.this.mSelectBox.setClickable(true);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsGroupChat = false;
    private boolean mIsOriginalSize = false;
    private boolean mIsRcs;
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton view, boolean checked) {
            if (GalleryCompressFragment.this.mGalleryCompressAdapter != null) {
                String path = GalleryCompressFragment.this.mGalleryCompressAdapter.getPicturePath(GalleryCompressFragment.this.mCurrentPage);
                String contentType = GalleryCompressFragment.this.mGalleryCompressAdapter.getContentType(GalleryCompressFragment.this.mCurrentPage);
                if (GalleryCompressFragment.this.getRichMessageEditor(GalleryCompressFragment.this.mActivityId) == null || TextUtils.isEmpty(path) || TextUtils.isEmpty(contentType)) {
                    GalleryCompressFragment.this.checkRcsGroupPictureItem(checked, path, contentType);
                } else {
                    GalleryCompressFragment.this.checkPictureItem(checked, path, contentType);
                }
                GalleryCompressFragment.this.mSelectBox.setClickable(false);
                if (checked) {
                    if (GalleryCompressFragment.this.mIsRcs) {
                        GalleryCompressFragment.this.updateWarningDialog(GalleryCompressFragment.this.getContext());
                    }
                    GalleryCompressFragment.this.mHanderEx.sendEmptyMessageDelayed(10002, 800);
                } else {
                    GalleryCompressFragment.this.mHanderEx.sendEmptyMessageDelayed(10002, 300);
                }
            }
        }
    };
    private OnClickListener mOnclickListener = new OnClickListener() {
        public void onClick(View v) {
            GalleryCompressFragment.this.updapteFullSize(true);
            RichMessageEditor richMessageEditor = GalleryCompressFragment.this.getRichMessageEditor(GalleryCompressFragment.this.mActivityId);
            if (richMessageEditor != null) {
                richMessageEditor.setFullSizeFlag(GalleryCompressFragment.this.mIsOriginalSize);
                return;
            }
            RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor = GalleryCompressFragment.this.getRcsGroupChatRichMessageEditor(GalleryCompressFragment.this.mActivityId);
            if (rcsGroupChatRichMessageEditor != null) {
                rcsGroupChatRichMessageEditor.setFullSizeFlag(GalleryCompressFragment.this.mIsOriginalSize);
            }
        }
    };
    private String mPathString = null;
    private View mRootView = null;
    private CheckBox mSelectBox = null;
    private SendModeSetListener mSendModeListener = new SendModeSetListener() {
        public void onSendModeSet(boolean isRcsMode, boolean isSendModeLocked) {
            GalleryCompressFragment.this.mIsRcs = isRcsMode;
            GalleryCompressFragment.this.updateRcsMode();
        }

        public int autoSetSendMode(boolean ignoreCapTimeOut, boolean ignoreLoginStatus) {
            int retVal = 0;
            if (RcsProfile.isRcsServiceEnabledAndUserLogin() || ignoreLoginStatus) {
                boolean isImAvailable = RcsProfile.isImAvailable(GalleryCompressFragment.this.mSendPhoneNumber, ignoreCapTimeOut);
                MLog.d("GalleryCompressFragment", "autoSetSendMode isImAvailable:" + isImAvailable);
                if (isImAvailable) {
                    retVal = 1;
                }
            }
            MLog.d("GalleryCompressFragment", "autoSetSendMode return:" + retVal);
            return retVal;
        }
    };
    private String mSendPhoneNumber;
    private int mTotalPages;
    private WarningDialog mWarningDialog = null;
    private TextView mfileSizeView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            this.mActivityId = intent.getIntExtra("gallery-compress-activity", -1);
            this.mCurrentPage = intent.getIntExtra("gallery-compress-pisition", 1);
            this.mIsRcs = intent.getBooleanExtra("gallery-compress-rcs", false);
            this.mSendPhoneNumber = intent.getStringExtra("phoneNumber");
            this.mIsGroupChat = intent.getBooleanExtra("isGroupChat", false);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.gallery_compress_fragment, null, false);
        return this.mRootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GalleryCursorManager.get().registeGalleryCursorListener(this.mGalleryCursorListener);
        RichMessageEditor richMessageEditor = getRichMessageEditor(this.mActivityId);
        RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor = getRcsGroupChatRichMessageEditor(this.mActivityId);
        if (richMessageEditor != null) {
            richMessageEditor.addRichAttachmentListener(this);
        }
        if (rcsGroupChatRichMessageEditor != null) {
            rcsGroupChatRichMessageEditor.addRcsGroupRichAttachmentListener(this);
        }
        this.mSelectBox = (CheckBox) this.mRootView.findViewById(R.id.gallery_compress_checkbox);
        this.mSelectBox.setOnCheckedChangeListener(this.mOnCheckedChangeListener);
        this.mCurrentPageTextView = (TextView) this.mRootView.findViewById(R.id.gallery_compress_current_page);
        this.mBtnBackView = (ImageView) this.mRootView.findViewById(R.id.gallery_compress_btn_back);
        this.mBtnBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                GalleryCompressFragment.this.getActivity().finish();
            }
        });
        this.mFileInfoView = this.mRootView.findViewById(R.id.fileInfoView);
        this.mBarHeightPort = getResources().getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android"));
        updateRcsMode();
        this.mFullSizeRadioButton = (RadioButton) this.mRootView.findViewById(R.id.imageView2);
        this.mfileSizeView = (TextView) this.mRootView.findViewById(R.id.textView1);
        this.mFullSizeRadioButton.setOnClickListener(this.mOnclickListener);
        this.mfileSizeView.setOnClickListener(this.mOnclickListener);
        this.mGalleryCompressViewPager = (ViewPager) this.mRootView.findViewById(R.id.gallery_compress_viewpager);
        if (GalleryCursorManager.get().isGalleryCursorVailbie()) {
            this.mTotalPages = GalleryCursorManager.get().getGalleryCursorCount() - 1;
            setCurrentPageText(this.mCurrentPage, this.mTotalPages);
            this.mGalleryCompressAdapter = new GalleryCompressAdapter(getContext(), GalleryCursorManager.get().getGalleryCursor());
            this.mGalleryCompressViewPager.setAdapter(this.mGalleryCompressAdapter);
            this.mGalleryCompressViewPager.setOnPageChangeListener(new OnPageChangeListener() {
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                public void onPageSelected(int position) {
                    GalleryCompressFragment.this.mCurrentPage = position + 1;
                    GalleryCompressFragment.this.updateVeiwStatus();
                    GalleryCompressFragment.this.setCurrentPageText(GalleryCompressFragment.this.mCurrentPage, GalleryCompressFragment.this.mTotalPages);
                }

                public void onPageScrollStateChanged(int state) {
                }
            });
            refreshViewPager(false);
            this.mGalleryCompressViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    GalleryCompressFragment.this.mGalleryCompressViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    GalleryCompressFragment.this.updateVeiwStatus();
                }
            });
            restoreFullSizeFlag();
            return;
        }
        MLog.e("GalleryCompressFragment", "GalleryCursor is unvailbie.");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mSelectBox.setPadding(0, 0, getActivity().getResources().getDimensionPixelSize(R.dimen.gallery_compress_checkbox_margin_end), 0);
        refreshBottomView();
    }

    public void onStart() {
        super.onStart();
        if (!this.mIsGroupChat) {
            RcseMmsExt.registerSendModeSetListener(this.mSendModeListener);
        }
    }

    public void onResume() {
        super.onResume();
        refreshBottomView();
    }

    public void refreshBottomView() {
        if (this.mFileInfoView != null) {
            int addHeight = 0;
            int hiddenNavStatus = Global.getInt(getActivity().getContentResolver(), "navigationbar_is_min", 0);
            boolean isSupportNav = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
            if (getResources().getConfiguration().orientation == 1 && isSupportNav && hiddenNavStatus == 0) {
                addHeight = this.mBarHeightPort;
            }
            LayoutParams layoutParams = this.mFileInfoView.getLayoutParams();
            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.gallery_compress_fileinfo_height) + addHeight;
            this.mFileInfoView.setLayoutParams(layoutParams);
        }
    }

    public void onStop() {
        if (!this.mIsGroupChat) {
            RcseMmsExt.unRegisterSendModeSetListener(this.mSendModeListener);
        }
        super.onStop();
    }

    public void onDestroy() {
        if (this.mGalleryCompressAdapter != null) {
            this.mGalleryCompressAdapter.destoryItemCache();
            this.mGalleryCompressAdapter = null;
        }
        this.mRootView = null;
        GalleryCursorManager.get().unregisterGalleryCursorListener(this.mGalleryCursorListener);
        RichMessageEditor richMessageEditor = getRichMessageEditor(this.mActivityId);
        RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor = getRcsGroupChatRichMessageEditor(this.mActivityId);
        if (richMessageEditor != null) {
            richMessageEditor.removeRichAttachmentListener(this);
        }
        if (rcsGroupChatRichMessageEditor != null) {
            rcsGroupChatRichMessageEditor.removeRcsGroupRichAttachmentListener(this);
        }
        super.onDestroy();
    }

    private RichMessageEditor getRichMessageEditor(int taskId) {
        return RichMessageManager.get().getRichMessageEditor(this.mActivityId);
    }

    private void checkPictureItem(boolean isChecked, String path, String contentType) {
        int i = 2;
        RichMessageEditor richMessageEditor = getRichMessageEditor(this.mActivityId);
        if (richMessageEditor != null && !TextUtils.isEmpty(path) && !TextUtils.isEmpty(contentType)) {
            try {
                Uri uri = Uri.fromFile(new File(path));
                if (isChecked) {
                    richMessageEditor.setDiscarded(false);
                    Activity activity = getActivity();
                    if (!contentType.startsWith("image")) {
                        i = 5;
                    }
                    richMessageEditor.setNewAttachment(activity, uri, i, false);
                } else {
                    if (!contentType.startsWith("image")) {
                        i = 5;
                    }
                    richMessageEditor.removeImageAttachment(uri, i);
                }
            } catch (Exception e) {
                MLog.e("GalleryCompressFragment", "addImageAttachment failed," + e.getMessage());
            }
        }
    }

    private RcsGroupChatRichMessageEditor getRcsGroupChatRichMessageEditor(int taskId) {
        return RichMessageManager.get().getRcsGroupChatRichMessageEditor(this.mActivityId);
    }

    private void checkRcsGroupPictureItem(boolean isChecked, String path, String contentType) {
        int i = 2;
        RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor = getRcsGroupChatRichMessageEditor(this.mActivityId);
        if (rcsGroupChatRichMessageEditor != null && !TextUtils.isEmpty(path) && !TextUtils.isEmpty(contentType)) {
            try {
                Uri uri = Uri.fromFile(new File(path));
                if (isChecked) {
                    if (!contentType.startsWith("image")) {
                        i = 5;
                    }
                    rcsGroupChatRichMessageEditor.setNewAttachment(uri, i);
                } else {
                    if (!contentType.startsWith("image")) {
                        i = 5;
                    }
                    rcsGroupChatRichMessageEditor.removeData(uri, i);
                }
            } catch (Exception e) {
                MLog.e("GalleryCompressFragment", "addImageAttachment failed," + e.getMessage());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isItemSelected(String path) {
        RichMessageEditor richMessageEditor = getRichMessageEditor(this.mActivityId);
        if (richMessageEditor == null || richMessageEditor.getWorkingMessage() == null || richMessageEditor.getWorkingMessage().getSlideshow() == null || !richMessageEditor.getWorkingMessageSlideImageSourceBuilds().contains(path)) {
            return false;
        }
        return true;
    }

    private boolean isRcsItemSelected(String path) {
        RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor = getRcsGroupChatRichMessageEditor(this.mActivityId);
        if (rcsGroupChatRichMessageEditor != null && rcsGroupChatRichMessageEditor.getSourceBuildsData().contains(path)) {
            return true;
        }
        return false;
    }

    private void setCurrentPageText(int currentPage, int totalPage) {
        if (this.mCurrentPageTextView != null) {
            this.mCurrentPageTextView.setText(currentPage + "/" + totalPage);
        }
    }

    private void refreshViewPager(boolean isScroll) {
        if (this.mTotalPages != 0) {
            if (this.mCurrentPage > this.mTotalPages) {
                this.mCurrentPage = 1;
            }
            if (this.mGalleryCompressViewPager != null) {
                this.mGalleryCompressViewPager.setCurrentItem(this.mCurrentPage - 1, isScroll);
            }
            updateVeiwStatus();
        }
    }

    public void onRichAttachmentChanged(int changedType) {
        if (changedType == 2 || changedType == 5) {
            this.mHanderEx.sendEmptyMessage(10001);
        }
    }

    public void onRcsGroupChatRichAttachmentChanged(int changedType) {
        if (changedType == 2 || changedType == 5) {
            this.mHanderEx.sendEmptyMessage(10003);
        }
    }

    public void updateVeiwStatus() {
        if (this.mGalleryCompressAdapter != null) {
            String pathString = this.mGalleryCompressAdapter.getPicturePath(this.mCurrentPage);
            if (!TextUtils.isEmpty(pathString) && this.mSelectBox != null) {
                RichMessageEditor richMessageEditor = getRichMessageEditor(this.mActivityId);
                this.mSelectBox.setOnCheckedChangeListener(null);
                if (richMessageEditor != null) {
                    this.mSelectBox.setChecked(isItemSelected(pathString));
                } else {
                    this.mSelectBox.setChecked(isRcsItemSelected(pathString));
                }
                this.mSelectBox.setOnCheckedChangeListener(this.mOnCheckedChangeListener);
                updapteFullSize(false);
            }
        }
    }

    private void updateRcsMode() {
        this.mFileInfoView.setVisibility(this.mIsRcs ? 0 : 8);
    }

    private void restoreFullSizeFlag() {
        RichMessageEditor richMessageEditor = getRichMessageEditor(this.mActivityId);
        if (richMessageEditor != null) {
            this.mIsOriginalSize = richMessageEditor.getFullSizeFlag();
        } else {
            RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor = getRcsGroupChatRichMessageEditor(this.mActivityId);
            if (rcsGroupChatRichMessageEditor != null) {
                this.mIsOriginalSize = rcsGroupChatRichMessageEditor.getFullSizeFlag();
            }
        }
        updapteFullSize(false);
    }

    private void updapteFullSize(boolean changed) {
        if (changed) {
            this.mIsOriginalSize = !this.mIsOriginalSize;
        }
        this.mFullSizeRadioButton.setChecked(this.mIsOriginalSize);
        showOrHideFileSize(this.mIsOriginalSize);
    }

    private void showOrHideFileSize(boolean show) {
        if (this.mGalleryCompressAdapter != null) {
            String fileSize = this.mGalleryCompressAdapter.getFileSize(this.mCurrentPage);
            String str = getString(R.string.rbtn_text_full_size);
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            if (show) {
                sb.append("(");
                sb.append(fileSize);
                sb.append(")");
            }
            this.mfileSizeView.setText(sb.toString());
        }
    }

    private void updateWarningDialog(Context context) {
        if (this.mGalleryCompressAdapter != null) {
            String pathString = this.mGalleryCompressAdapter.getPicturePath(this.mCurrentPage);
            if (!(TextUtils.isEmpty(pathString) || pathString.equals(this.mPathString))) {
                this.mPathString = pathString;
                if (this.mWarningDialog == null || !this.mWarningDialog.isShowing()) {
                    this.mWarningDialog = WarningDialog.show(getChildFragmentManager(), getContext(), this.mPathString);
                }
            }
        }
    }
}
