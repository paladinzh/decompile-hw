package com.huawei.gallery.freeshare;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.freeshare.client.device.DeviceInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.app.AbsPhotoPage.Model;
import com.huawei.gallery.freeshare.FreeShareAdapter.Listener;
import com.huawei.gallery.util.UIUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import java.util.List;

public class FreeShareHost extends RelativeLayout implements OnItemClickListener, FreeShare, Listener {
    private static final int ACTION_CONTAINER_HEIGHT = GalleryUtils.dpToPixel(48);
    private static final int PLACE_HOLDER_COLOR = Color.rgb(48, 48, 48);
    private static final int SENSITIVITY = GalleryUtils.dpToPixel(50);
    private boolean isFreeShareHostFadeOutAnimationStarted;
    private RelativeLayout mActionContainer;
    private OnClickListener mActionListener;
    private DeviceListAdapter mAdapter;
    private ListView mDeviceList;
    private TextView mDeviceListTitle;
    private AlertDialog mDialog;
    private DialogInterface.OnClickListener mDialogListener;
    private float mDonwY;
    private float mDownX;
    private SimpleActionItem mExitAction;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private Animation mFlingOutAnimation;
    private Animation mFlyOutAnimation;
    private Animation mFreeShareHostFadeOutAnimation;
    private Handler mHandler;
    private RelativeLayout mHelpInfo;
    private boolean mHoldingDown;
    private ImageView mImageContainer;
    private CheckBox mInfoConfirmBox;
    private boolean mInfoShown;
    private boolean mIsSearching;
    private int mLaunchModeForBigData;
    private LinearLayout mListContainer;
    private int mListHeightMin;
    private Model mModel;
    private int mNavigationBarHeight;
    private boolean mNeedInfo;
    private ViewGroup mParent;
    private FreeShareAdapter mRemoteAdapter;
    private int mScreenHeight;
    private int mScreenWidth;
    private SimpleActionItem mSearchAction;
    private SearchingAnimation mSearchingAnimation;
    private View mSplitLineView;
    private TerminateDialog mTerminater;

    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<DeviceInfo> mDevices = new ArrayList();

        public DeviceListAdapter(List<DeviceInfo> list) {
            this.mDevices.addAll(list);
        }

        public synchronized void updateDevices(List<DeviceInfo> list) {
            this.mDevices.clear();
            this.mDevices.addAll(list);
            notifyDataSetChanged();
            if (!FreeShareHost.this.mIsSearching) {
                FreeShareHost.this.updateListTitle();
            }
        }

        public int getCount() {
            return this.mDevices.size();
        }

        public Object getItem(int position) {
            return this.mDevices.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View layout;
            if (convertView != null) {
                layout = convertView;
            } else {
                layout = LayoutInflater.from(FreeShareHost.this.getContext()).inflate(R.layout.layout_freeshare_list_item, parent, false);
                UIUtils.addStateListAnimation(layout, FreeShareHost.this.getContext());
            }
            return updateItem(layout, position);
        }

        private View updateItem(View layout, int position) {
            ((TextView) layout.findViewById(R.id.name)).setText(((DeviceInfo) this.mDevices.get(position)).getName());
            return layout;
        }
    }

    private class FadeOutListener implements AnimationListener {
        private FadeOutListener() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (FreeShareHost.this.getVisibility() != 0) {
                FreeShareHost.this.freeResource();
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class FlingOutListener implements AnimationListener {
        private FlingOutListener() {
        }

        public void onAnimationStart(Animation animation) {
            LayoutParams params = (LayoutParams) FreeShareHost.this.mImageContainer.getLayoutParams();
            params.topMargin = 0;
            params.height = FreeShareHost.this.mScreenHeight;
            FreeShareHost.this.updateViewLayout(FreeShareHost.this.mImageContainer, params);
        }

        public void onAnimationEnd(Animation animation) {
            MediaInfo mediaInfo = (MediaInfo) FreeShareHost.this.mImageContainer.getTag();
            if (!(TextUtils.isEmpty(mediaInfo.mimetype) || TextUtils.isEmpty(mediaInfo.uri))) {
                FreeShareHost.this.mRemoteAdapter.sendMedia(mediaInfo.uri, mediaInfo.mimetype);
            }
            int actionHeight = FreeShareHost.this.mActionContainer.getMeasuredHeight();
            int spaceLeft = (FreeShareHost.this.mScreenHeight - actionHeight) - FreeShareHost.this.mListContainer.getMeasuredHeight();
            LayoutParams params = (LayoutParams) FreeShareHost.this.mImageContainer.getLayoutParams();
            params.topMargin = actionHeight;
            params.height = spaceLeft;
            FreeShareHost.this.updateViewLayout(FreeShareHost.this.mImageContainer, params);
            FreeShareHost.this.mImageContainer.setVisibility(4);
            FreeShareHost.this.startFreeShareHostHideAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class FlingUpAnimator extends ValueAnimator implements AnimatorUpdateListener, AnimatorListener {
        private int mDiffInHeight;
        private int mTopMargin = FreeShareHost.ACTION_CONTAINER_HEIGHT;

        public FlingUpAnimator() {
            this.mDiffInHeight = FreeShareHost.this.mScreenHeight - ((int) (((float) FreeShareHost.this.mScreenHeight) * 0.5f));
            setFloatValues(new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1});
            setDuration(382);
            addListener(this);
            addUpdateListener(this);
        }

        public void onAnimationStart(Animator animation) {
            FreeShareHost.this.mImageContainer.setVisibility(0);
        }

        public void onAnimationEnd(Animator animation) {
            FreeShareHost.this.doSearch();
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float progress = ((Float) animation.getAnimatedValue()).floatValue();
            FreeShareHost.this.mImageContainer.setAlpha(progress);
            LayoutParams params = (LayoutParams) FreeShareHost.this.mImageContainer.getLayoutParams();
            params.height = (int) (((float) FreeShareHost.this.mScreenHeight) - (((float) this.mDiffInHeight) * progress));
            params.topMargin = (int) (((float) this.mTopMargin) * progress);
            FreeShareHost.this.updateViewLayout(FreeShareHost.this.mImageContainer, params);
        }
    }

    private class FreeShareHostFadeOutListener extends FadeOutListener {
        private FreeShareHostFadeOutListener() {
            super();
        }

        public void onAnimationEnd(Animation animation) {
            if (FreeShareHost.this.getVisibility() == 0) {
                FreeShareHost.this.setVisibility(4);
            }
            FreeShareHost.this.isFreeShareHostFadeOutAnimationStarted = false;
            super.onAnimationEnd(animation);
        }
    }

    private static class MediaInfo {
        private String mimetype;
        private String uri;

        public MediaInfo(MediaItem item) {
            if (item.getMediaType() != 1 && item.getContentUri() != null) {
                this.uri = item.getContentUri().toString();
                this.mimetype = item.getMimeType();
            }
        }

        public MediaInfo() {
            this.uri = "";
            this.mimetype = "";
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    FreeShareHost.this.hide();
                    return;
                case 2:
                    FreeShareHost.this.cancelSearch();
                    return;
                case 3:
                    FreeShareHost.this.mAdapter.updateDevices(FreeShareHost.this.mRemoteAdapter.getDeviceList());
                    return;
                default:
                    return;
            }
        }
    }

    private class SearchingAnimation implements Runnable {
        private int mCount = 0;
        private String mSearchingString;

        public SearchingAnimation(Context context) {
            this.mSearchingString = context.getString(R.string.freeshare_list_title_searhing);
        }

        public void stop() {
            FreeShareHost.this.mHandler.removeCallbacks(this);
            this.mCount = 0;
        }

        public void run() {
            if (FreeShareHost.this.mIsSearching) {
                String string;
                switch (this.mCount % 4) {
                    case 1:
                        string = " " + this.mSearchingString + ".";
                        break;
                    case 2:
                        string = "  " + this.mSearchingString + "..";
                        break;
                    case 3:
                        string = "   " + this.mSearchingString + "...";
                        break;
                    default:
                        string = this.mSearchingString;
                        break;
                }
                this.mCount++;
                FreeShareHost.this.mDeviceListTitle.setText(string);
                FreeShareHost.this.mHandler.postDelayed(this, 600);
            }
        }
    }

    private class TerminateDialog extends AlertDialog implements DialogInterface.OnClickListener {
        protected TerminateDialog(Context context) {
            super(context);
            setButton(-1, context.getString(R.string.freeshare_terminate_title), this);
            setButton(-2, context.getString(R.string.cancel), this);
        }

        public void trigger(String name) {
            setTitle(getContext().getString(R.string.freeshare_terminate_title));
            setMessage(getContext().getString(R.string.freeshare_terminate_message, new Object[]{name}));
            show();
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    FreeShareHost.this.mRemoteAdapter.selectDevice(null);
                    FreeShareHost.this.mRemoteAdapter.cancelShare();
                    return;
                default:
                    return;
            }
        }
    }

    public FreeShareHost(Context context) {
        this(context, null);
    }

    public FreeShareHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FreeShareHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.isFreeShareHostFadeOutAnimationStarted = false;
        this.mInfoShown = false;
        this.mIsSearching = false;
        this.mDialog = null;
        this.mActionListener = new OnClickListener() {
            private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;

            private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
                if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
                    return -com-huawei-gallery-actionbar-ActionSwitchesValues;
                }
                int[] iArr = new int[Action.values().length];
                try {
                    iArr[Action.ADD.ordinal()] = 4;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[Action.ADD_ALBUM.ordinal()] = 5;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[Action.ADD_COMMENT.ordinal()] = 6;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[Action.AIRSHARE.ordinal()] = 7;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[Action.ALBUM.ordinal()] = 8;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[Action.ALL.ordinal()] = 9;
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
                    iArr[Action.INFO.ordinal()] = 1;
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
                    iArr[Action.NO.ordinal()] = 2;
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
                    iArr[Action.OK.ordinal()] = 39;
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
                    iArr[Action.RE_SEARCH.ordinal()] = 3;
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

            public void onClick(View v) {
                if (v instanceof SimpleActionItem) {
                    switch (AnonymousClass1.-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[((SimpleActionItem) v).getAction().ordinal()]) {
                        case 1:
                            FreeShareHost.this.cancelSearch();
                            FreeShareHost.this.clearAnimation();
                            FreeShareHost.this.startHiding(FreeShareHost.this.mActionContainer, FreeShareHost.this.mImageContainer, FreeShareHost.this.mListContainer);
                            FreeShareHost.this.showInfo(3);
                            return;
                        case 2:
                            FreeShareHost.this.doHide();
                            return;
                        case 3:
                            FreeShareHost.this.doSearch();
                            return;
                        default:
                            return;
                    }
                }
            }
        };
        this.mDialogListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    FreeShareHost.this.flingUpwithHideInfo();
                } else {
                    FreeShareHost.this.hide();
                }
                FreeShareHost.this.mNeedInfo = !FreeShareHost.this.mInfoConfirmBox.isChecked();
                PreferenceManager.getDefaultSharedPreferences(FreeShareHost.this.getContext()).edit().putBoolean("key-need-info", FreeShareHost.this.mNeedInfo).commit();
                FreeShareHost.this.mDialog.dismiss();
                FreeShareHost.this.mInfoShown = false;
            }
        };
        FreeShareUtils.init(context);
        initAnim(context);
    }

    private void initAnim(Context context) {
        this.mFadeInAnimation = AnimationUtils.loadAnimation(context, 17432576);
        this.mFadeInAnimation.setFillAfter(true);
        this.mFadeOutAnimation = AnimationUtils.loadAnimation(context, 17432577);
        this.mFadeOutAnimation.setAnimationListener(new FadeOutListener());
        this.mFreeShareHostFadeOutAnimation = AnimationUtils.loadAnimation(context, 17432577);
        this.mFreeShareHostFadeOutAnimation.setAnimationListener(new FreeShareHostFadeOutListener());
        this.isFreeShareHostFadeOutAnimationStarted = false;
        this.mFlingOutAnimation = AnimationUtils.loadAnimation(context, R.anim.freeshare_fling_out);
        this.mFlingOutAnimation.setAnimationListener(new FlingOutListener());
        this.mFlyOutAnimation = AnimationUtils.loadAnimation(context, R.anim.freeshare_fly_out);
        this.mSearchingAnimation = new SearchingAnimation(context);
    }

    private void startHiding(View... views) {
        for (View view : views) {
            startHideAnimation(view);
        }
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == 0) {
            view.startAnimation(this.mFadeOutAnimation);
            view.setVisibility(4);
        }
    }

    private void startFreeShareHostHideAnimation() {
        if (getVisibility() == 0 && !this.isFreeShareHostFadeOutAnimationStarted) {
            this.isFreeShareHostFadeOutAnimationStarted = true;
            startAnimation(this.mFreeShareHostFadeOutAnimation);
        }
    }

    private void startShowing(View... views) {
        for (View view : views) {
            startShowAnimation(view);
        }
    }

    private void startShowAnimation(View view) {
        if (view.getVisibility() != 0) {
            view.startAnimation(this.mFadeInAnimation);
            view.setVisibility(0);
        }
    }

    public FreeShareHost(Context context, FreeShareAdapter adapter, ViewGroup root) {
        this(context);
        this.mRemoteAdapter = (FreeShareAdapter) Utils.checkNotNull(adapter);
        this.mHandler = new MyHandler();
        this.mTerminater = new TerminateDialog(context);
        this.mNeedInfo = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key-need-info", true);
        this.mParent = root;
        this.mParent.addView(this, new LayoutParams(-1, -1));
        initializeViews(context);
        initializeData();
    }

    private void initializeViews(Context context) {
        updateParams();
        this.mActionContainer = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.headview_state_action, this.mParent, false);
        this.mExitAction = (SimpleActionItem) this.mActionContainer.findViewById(R.id.head_select_left);
        this.mExitAction.applyStyle(1);
        this.mExitAction.setAction(Action.NO);
        this.mExitAction.setOnClickListener(this.mActionListener);
        this.mSearchAction = (SimpleActionItem) this.mActionContainer.findViewById(R.id.head_select_right);
        this.mSearchAction.applyStyle(1);
        this.mSearchAction.setAction(Action.RE_SEARCH);
        this.mSearchAction.setEnabled(false);
        this.mSearchAction.setOnClickListener(this.mActionListener);
        this.mSplitLineView = this.mActionContainer.findViewById(R.id.head_bar_split_line);
        if (this.mSplitLineView != null) {
            this.mSplitLineView.setVisibility(0);
        }
        ((TextView) this.mActionContainer.findViewById(R.id.head_actionmode_title)).setText(R.string.freeshare_title);
        LayoutParams params = new LayoutParams(-1, ACTION_CONTAINER_HEIGHT);
        params.addRule(10);
        this.mActionContainer.setLayoutParams(params);
        this.mActionContainer.setVisibility(4);
        this.mActionContainer.measure(0, 0);
        addView(this.mActionContainer);
        this.mImageContainer = new ImageView(context);
        this.mImageContainer.setScaleType(ScaleType.FIT_CENTER);
        this.mImageContainer.setAdjustViewBounds(true);
        params = new LayoutParams(-1, -2);
        params.addRule(14);
        this.mImageContainer.setLayoutParams(params);
        this.mImageContainer.setVisibility(4);
        addView(this.mImageContainer);
        this.mListContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.layout_freeshare_device_list, this.mParent, false);
        this.mDeviceListTitle = (TextView) this.mListContainer.findViewById(R.id.device_list_title);
        this.mDeviceList = (ListView) this.mListContainer.findViewById(R.id.device_list);
        this.mDeviceList.setOnItemClickListener(this);
        params = new LayoutParams(-1, this.mListHeightMin);
        params.addRule(12);
        this.mListContainer.setLayoutParams(params);
        this.mListContainer.setVisibility(4);
        addView(this.mListContainer);
        this.mListContainer.measure(0, 0);
        this.mListContainer.requestLayout();
        setVisibility(8);
        requestLayout();
    }

    private void createDialog() {
        Context context = getContext();
        this.mDialog = new Builder(context).setTitle(R.string.freeshare_helper_title).setCancelable(false).setNegativeButton(R.string.cancel, this.mDialogListener).setPositiveButton(R.string.freeshare_helper_button_start_share, this.mDialogListener).create();
        this.mHelpInfo = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_freeshare_helper, this.mParent, false);
        this.mInfoConfirmBox = (CheckBox) this.mHelpInfo.findViewById(R.id.checker);
        this.mHelpInfo.measure(0, 0);
        LayoutParams params = new LayoutParams(-2, -2);
        params.addRule(13);
        this.mHelpInfo.setLayoutParams(params);
        this.mDialog.setCancelable(false);
        this.mDialog.setView(this.mHelpInfo);
    }

    private void setDefaultBackground() {
        setBackground(new ColorDrawable(-16777216));
    }

    private void updateParams() {
        this.mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        this.mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        this.mListHeightMin = (this.mScreenHeight / 2) - ACTION_CONTAINER_HEIGHT;
    }

    private void updateContainerLayout() {
        LayoutParams params = (LayoutParams) this.mActionContainer.getLayoutParams();
        params.rightMargin = isPort() ? 0 : this.mNavigationBarHeight;
        this.mActionContainer.setLayoutParams(params);
    }

    private void updateImageLayout() {
        LayoutParams params = (LayoutParams) this.mImageContainer.getLayoutParams();
        params.height = (int) (((float) this.mScreenHeight) * 0.5f);
        this.mImageContainer.setPadding(0, 0, isPort() ? 0 : this.mNavigationBarHeight, 0);
        updateViewLayout(this.mImageContainer, params);
    }

    private void updateListLayout() {
        int i;
        LayoutParams params = (LayoutParams) this.mListContainer.getLayoutParams();
        params.height = this.mListHeightMin;
        params.rightMargin = isPort() ? 0 : this.mNavigationBarHeight;
        if (isPort()) {
            i = this.mNavigationBarHeight;
        } else {
            i = 0;
        }
        params.bottomMargin = i;
        updateViewLayout(this.mListContainer, params);
        this.mListContainer.measure(0, 0);
    }

    private void initializeData() {
        this.mRemoteAdapter.addListener(this);
        this.mAdapter = new DeviceListAdapter(this.mRemoteAdapter.getDeviceList());
        this.mDeviceList.setAdapter(this.mAdapter);
        this.mDeviceList.setOnItemClickListener(this);
    }

    private void flingUpwithHideInfo() {
        setDefaultBackground();
        clearAnimation();
        new FlingUpAnimator().start();
        startShowing(this.mActionContainer, this.mListContainer);
        this.mInfoShown = false;
        ReportToBigData.reportFreeshareLaunchMode(this.mLaunchModeForBigData);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mRemoteAdapter.selectDevice((DeviceInfo) this.mAdapter.getItem(position));
        MediaInfo mediaInfo = (MediaInfo) this.mImageContainer.getTag();
        if (!(TextUtils.isEmpty(mediaInfo.mimetype) || TextUtils.isEmpty(mediaInfo.uri))) {
            this.mRemoteAdapter.sendMedia(mediaInfo.uri, mediaInfo.mimetype);
        }
        this.mImageContainer.startAnimation(this.mFlyOutAnimation);
        this.mImageContainer.setVisibility(4);
        startHiding(this.mActionContainer, this.mListContainer, this);
        cancelSearch();
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (!this.mHoldingDown) {
                    this.mHoldingDown = true;
                    this.mDonwY = event.getY();
                    this.mDownX = event.getX();
                    break;
                }
                break;
            case 1:
                if (this.mHoldingDown) {
                    this.mHoldingDown = false;
                    float upX = event.getX();
                    float distanceY = event.getY() - this.mDonwY;
                    if ((Math.abs(upX - this.mDownX) <= ((float) SENSITIVITY) && distanceY >= ((float) SENSITIVITY)) || this.mInfoShown) {
                        this.mHandler.sendEmptyMessage(2);
                        this.mHandler.sendEmptyMessage(1);
                        break;
                    }
                    doSearch();
                    break;
                }
                break;
        }
        return true;
    }

    private boolean isPort() {
        return getResources().getConfiguration().orientation == 1;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        clearAnimation();
        updateParams();
        updateContainerLayout();
        updateListLayout();
        updateImageLayout();
    }

    public void doShow(int launchmode, boolean supportShare) {
        this.mLaunchModeForBigData = launchmode;
        if (launchmode == 4 && !this.mRemoteAdapter.hasTargetDevice()) {
            return;
        }
        if (!supportShare) {
            ContextedUtils.showToastQuickly(getContext(), getResources().getString(R.string.do_not_support, new Object[]{getResources().getString(R.string.freeshare_title)}), 0);
        } else if (prepareResource()) {
            clearAnimation();
            startShowing(this);
            if (!flingOut()) {
                if (this.mNeedInfo) {
                    showInfo(launchmode);
                } else {
                    setDefaultBackground();
                    new FlingUpAnimator().start();
                    startShowing(this.mActionContainer, this.mListContainer);
                    ReportToBigData.reportFreeshareLaunchMode(this.mLaunchModeForBigData);
                }
            }
        }
    }

    private boolean prepareResource() {
        if (this.mModel == null) {
            GalleryLog.v("FreeShare_Host", "model is dirty, cannot prepare media!");
            return false;
        }
        MediaItem mediaItem = this.mModel.getMediaItem(0);
        this.mImageContainer.setTag(mediaItem == null ? new MediaInfo() : new MediaInfo(mediaItem));
        int rotation = this.mModel.getImageRotation(0);
        ScreenNail screenNail = this.mModel.getScreenNail();
        if (screenNail == null || !(screenNail instanceof TiledScreenNail)) {
            return false;
        }
        Bitmap bitmap = ((TiledScreenNail) screenNail).getBitmap();
        if (bitmap == null || bitmap.isRecycled()) {
            GalleryLog.w("FreeShare_Host", "invalid bitmap");
            this.mImageContainer.setImageDrawable(new ColorDrawable(PLACE_HOLDER_COLOR));
        } else {
            this.mImageContainer.setImageBitmap(BitmapUtils.rotateBitmap(BitmapUtils.resizeDownBySideLength(bitmap.copy(bitmap.getConfig(), bitmap.isMutable()), Math.min(this.mScreenHeight, this.mScreenWidth), true), rotation, true));
        }
        return true;
    }

    private void showInfo(int launchmode) {
        createDialog();
        TextView message = (TextView) this.mHelpInfo.findViewById(R.id.message);
        if (message != null) {
            switch (launchmode) {
                case 1:
                    message.setText(R.string.freeshare_helper_message_click);
                    break;
                case 2:
                    message.setText(R.string.freeshare_helper_message_slide);
                    break;
                case 3:
                    message.setText(R.string.freeshare_helper_message_info);
                    break;
            }
        }
        this.mDialog.show();
        this.mInfoShown = true;
    }

    private boolean flingOut() {
        if (!this.mRemoteAdapter.hasTargetDevice()) {
            return false;
        }
        this.mImageContainer.startAnimation(this.mFlingOutAnimation);
        return true;
    }

    public void doHide() {
        cancelSearch();
        hide();
    }

    private void hide() {
        clearAnimation();
        if (!this.mInfoShown) {
            startHiding(this.mActionContainer, this.mImageContainer, this.mListContainer);
        }
        startHiding(this);
    }

    public boolean isShowing() {
        return isShown();
    }

    public void setModel(Model model) {
        this.mModel = model;
    }

    public boolean doCancel(OnDismissListener l) {
        if (!this.mRemoteAdapter.hasTargetDevice()) {
            return false;
        }
        this.mTerminater.setOnDismissListener(l);
        this.mTerminater.trigger(this.mRemoteAdapter.getTargetName());
        return true;
    }

    public void doClean() {
        clearAnimation();
        this.mRemoteAdapter.removeListener(this);
        this.mHandler.removeCallbacksAndMessages(null);
        this.mExitAction.setOnClickListener(null);
        this.mSearchAction.setOnClickListener(null);
        this.mDeviceList.setOnItemClickListener(null);
        this.mParent.removeView(this);
    }

    private void doSearch() {
        this.mIsSearching = true;
        this.mDeviceListTitle.setText(R.string.freeshare_list_title_searhing);
        this.mSearchAction.setEnabled(false);
        this.mRemoteAdapter.discover();
        this.mHandler.post(this.mSearchingAnimation);
    }

    private void cancelSearch() {
        this.mIsSearching = false;
        this.mSearchAction.setEnabled(true);
        this.mSearchingAnimation.stop();
        this.mRemoteAdapter.cancelDiscover();
        updateListTitle();
    }

    private void updateListTitle() {
        int count = this.mAdapter.getCount();
        this.mDeviceListTitle.setText(getResources().getQuantityString(R.plurals.freeshare_list_title_found, count, new Object[]{Integer.valueOf(count)}));
    }

    public void onDeviceChange() {
        if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessageDelayed(3, 500);
        }
    }

    public void onDiscoverFinished() {
        if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessageDelayed(3, 500);
        }
        this.mHandler.sendEmptyMessage(2);
    }

    public void onFinish() {
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mNavigationBarHeight = height;
        updateContainerLayout();
        updateListLayout();
        updateImageLayout();
    }

    private void freeResource() {
        this.mImageContainer.setImageBitmap(null);
        setBackground(null);
    }
}
