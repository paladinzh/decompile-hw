package com.android.keyguard.hwlockscreen.magazine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$color;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.android.keyguard.R$string;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.database.ClientHelper;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.BigPicture;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.KeyguardToast;
import com.huawei.keyguard.util.KeyguardUtils;
import com.huawei.keyguard.util.LocalLinkTextView;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.view.widget.ToolBoxView;
import com.huawei.openalliance.ad.inter.constant.EventType;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.nio.charset.Charset;

public class MagazineControlView extends LinearLayout implements OnClickListener, IContentListener {
    private int checkedNewVersion = -1;
    private boolean mChildButtonClickable = false;
    private boolean mCustomStatus = false;
    private boolean mDefaultStatus = false;
    private boolean mFavoriteStatus = false;
    private Handler mHandler = GlobalContext.getUIHandler();
    private TextView mMagazineDetail;
    private TextView mMagazineEmpty;
    private ImageView mMagazineFavorite;
    private long mNextClickTime = 0;
    private AsyncTask mPicShareTask = null;
    private AlertDialog mRemoveDialog;
    private View mRemoveView;
    private View mSettingsView;
    private View mShareView;
    private ImageView mSwitchView;
    private String mTitleName = BuildConfig.FLAVOR;
    private Runnable mUpdateControlState = new Runnable() {
        public void run() {
            MagazineControlView.this.setCustomedStatusView(MagazineUtils.isUserCustomedWallpaper(MagazineControlView.this.mContext), MagazineWallpaper.getInst(MagazineControlView.this.mContext).getPictureInfo(0) == null);
        }
    };
    private Runnable mUpdateStateView = new Runnable() {
        public void run() {
            int i;
            View updateLogo = null;
            int i2 = 0;
            MagazineControlView magazineControlView = MagazineControlView.this;
            if (ClientHelper.getInstance().hasCheckedNewVersion(MagazineControlView.this.getContext())) {
                i = 1;
            } else {
                i = 0;
            }
            magazineControlView.checkedNewVersion = i;
            if (MagazineControlView.this.isAttachedToWindow()) {
                updateLogo = MagazineControlView.this.findViewById(R$id.magazine_info_update_logo);
            }
            if (updateLogo != null) {
                if (MagazineControlView.this.checkedNewVersion != 1) {
                    i2 = 8;
                }
                updateLogo.setVisibility(i2);
            }
        }
    };

    public MagazineControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean ret = super.dispatchTouchEvent(ev);
        HwLog.w("MagazineControlView", "MagazineControlView dispatchTouchEvent " + ret + " " + ev.getActionMasked());
        return ret;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMagazineEmpty = findTextViewById(R$id.magazine_info_empty);
        this.mMagazineDetail = findTextViewById(R$id.magazine_info_detail);
        this.mShareView = setDefaultOnClickListener(R$id.magazine_info_share);
        this.mRemoveView = setDefaultOnClickListener(R$id.magazine_info_remove);
        this.mSettingsView = setDefaultOnClickListener(R$id.magazine_info_settings);
        if (!(this.mSettingsView == null || KeyguardCfg.isNetworkEnabled())) {
            this.mSettingsView.setVisibility(8);
        }
        this.mMagazineFavorite = (ImageView) setDefaultOnClickListener(R$id.magazine_cover_favorite);
        if (this.mMagazineFavorite != null) {
            if (KeyguardCfg.isMagazineUpdateEnabled()) {
                updateFavoritesStatus(false);
            } else {
                this.mMagazineFavorite.setVisibility(8);
                this.mMagazineFavorite.setEnabled(false);
            }
        }
        this.mSwitchView = (ImageView) setDefaultOnClickListener(R$id.magazine_info_switch);
        EventCenter.getInst().listenContent(8, this);
        this.mHandler.post(this.mUpdateStateView);
        updateContentDescription();
    }

    public void updateContentDescription() {
        this.mShareView.setContentDescription(getResources().getString(R$string.accessibility_magazine_info_share));
        this.mRemoveView.setContentDescription(getResources().getString(R$string.accessibility_magazine_info_remove));
        if (this.mSettingsView != null) {
            this.mSettingsView.setContentDescription(getResources().getString(R$string.accessibility_magazine_info_subscriptions_new));
        }
        if (this.mMagazineFavorite != null) {
            this.mMagazineFavorite.setContentDescription(getResources().getString(R$string.accessibility_magazine_info_favorite));
        }
        if (MagazineUtils.isAutoSwitchMagazine(getContext(), true)) {
            this.mSwitchView.setContentDescription(getContext().getText(R$string.accessibility_magazine_info_switc_pause));
        } else {
            this.mSwitchView.setContentDescription(getContext().getText(R$string.accessibility_magazine_info_switc_start));
        }
        ToolBoxView toolBox = (ToolBoxView) findViewById(R$id.bottom_tool_box);
        if (toolBox != null) {
            toolBox.updateContentDescription();
        }
    }

    private TextView findTextViewById(int id) {
        View view = findViewById(id);
        if (view instanceof TextView) {
            return (TextView) view;
        }
        return null;
    }

    private View setDefaultOnClickListener(int id) {
        View v = findViewById(id);
        if (v != null) {
            v.setOnClickListener(this);
        }
        return v;
    }

    public void onContentChange(boolean selfChange) {
        this.mHandler.post(this.mUpdateStateView);
    }

    public void setFavoriteStatus(boolean isEnabled) {
        BigPictureInfo info = null;
        if (this.mMagazineFavorite.isEnabled() && isEnabled) {
            this.mMagazineFavorite.setEnabled(false);
            setViewVisibility(this.mMagazineEmpty, 8);
        }
        if (!isEnabled) {
            BigPicture picture;
            if (MagazineUtils.isUserCustomedWallpaper(this.mContext)) {
                picture = null;
            } else {
                picture = MagazineWallpaper.getInst(this.mContext).getWallPaper(0);
            }
            if (!(picture == null || picture.isSameDrawable(KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper(), this.mContext))) {
                picture = null;
            }
            if (picture != null) {
                info = picture.getBigPictureInfo();
            }
            if (info != null) {
                updateMagazineFavoriteStatus(info.getFavoriteInfo(), info.getIsCustom());
            }
        }
    }

    public void setCustomedStatusView(boolean isCustomed, boolean isDefault) {
        boolean z;
        boolean z2 = false;
        boolean z3 = !isCustomed ? isDefault : true;
        if (!this.mCustomStatus && isCustomed) {
            MagazineUtils.setAutoSwitchMagazine(getContext(), false, "UserCustomedWallpaper change to false");
            boolean isAutoSwitch = MagazineUtils.isAutoSwitchMagazine(getContext(), true);
            this.mSwitchView.setImageResource(!isAutoSwitch ? R$drawable.ic_home_play : R$drawable.ic_home_pause);
            this.mSwitchView.setContentDescription(getContext().getText(!isAutoSwitch ? R$string.accessibility_magazine_info_switc_start : R$string.accessibility_magazine_info_switc_pause));
            setFavoriteStatus(true);
        } else if (this.mDefaultStatus || !isDefault) {
            setFavoriteStatus(false);
        } else {
            setFavoriteStatus(true);
        }
        ImageView imageView = (ImageView) findViewById(R$id.magazine_info_remove);
        if (z3) {
            z = false;
        } else {
            z = true;
        }
        imageView.setEnabled(z);
        imageView = (ImageView) findViewById(R$id.magazine_info_share);
        if (!z3) {
            z2 = true;
        }
        imageView.setEnabled(z2);
        this.mDefaultStatus = isDefault;
        this.mCustomStatus = isCustomed;
    }

    private void updateSwitchView() {
        if (MagazineUtils.isAutoSwitchMagazine(getContext(), true)) {
            this.mSwitchView.setImageResource(R$drawable.ic_home_pause);
            this.mSwitchView.setContentDescription(getContext().getText(R$string.accessibility_magazine_info_switc_pause));
            return;
        }
        this.mSwitchView.setImageResource(R$drawable.ic_home_play);
        this.mSwitchView.setContentDescription(getContext().getText(R$string.accessibility_magazine_info_switc_start));
    }

    private void updateFavoritesStatus(boolean revert) {
        if (revert) {
            this.mFavoriteStatus = !this.mFavoriteStatus;
        }
        if (this.mFavoriteStatus) {
            this.mMagazineFavorite.setImageResource(R$drawable.ic_public_favor);
        } else {
            this.mMagazineFavorite.setImageResource(R$drawable.ic_public_unfavor);
        }
    }

    public void onClick(View v) {
        int viewId = v.getId();
        if (this.mChildButtonClickable) {
            HwLog.v("MagazineControlView", "Click magazine panel: " + viewId);
            if (R$id.magazine_info_switch == viewId) {
                HwUnlockUtils.vibrate(this.mContext);
                respondSwitch();
            }
            if (R$id.magazine_info_settings == viewId) {
                HwUnlockUtils.vibrate(this.mContext);
                respondSettings();
                HwLockScreenReporter.report(this.mContext, 107, BuildConfig.FLAVOR);
                return;
            } else if (R$id.magazine_info_remove == viewId) {
                HwUnlockUtils.vibrate(this.mContext);
                respondRemove();
                return;
            } else if (R$id.magazine_info_share == viewId) {
                HwUnlockUtils.vibrate(this.mContext);
                respondShare();
                BigPictureInfo picInfo = MagazineWallpaper.getInst(this.mContext).getPictureInfo(0);
                if (isBigPictureInfoValid(picInfo)) {
                    HwLockScreenReporter.report(this.mContext, 112, "{share: " + picInfo.getPicUniqueName() + ",channel:" + picInfo.getChannelId() + "}");
                }
                return;
            } else if (R$id.magazine_cover_favorite == viewId) {
                HwUnlockUtils.vibrate(this.mContext);
                respondFavorite();
                return;
            } else {
                return;
            }
        }
        HwLog.w("MagazineControlView", "skip click magazine info view!");
    }

    private boolean isClickedTooQuickly() {
        boolean isTooQuickly = SystemClock.uptimeMillis() < this.mNextClickTime;
        HwLog.d("MagazineControlView", "isClickedTooQuickly " + isTooQuickly);
        return isTooQuickly;
    }

    protected void respondRemove() {
        if (isClickedTooQuickly()) {
            HwLog.w("MagazineControlView", "click remove too quickly");
            return;
        }
        this.mNextClickTime = SystemClock.uptimeMillis() + 300;
        Context context = MagazineUtils.getHwThemeContext(this.mContext, "androidhwext:style/Theme.Emui.Dialog");
        if (context == null) {
            HwLog.w("MagazineControlView", "respond remove with origin theme ");
            context = this.mContext;
        }
        View messageView = View.inflate(context, R$layout.magazine_dialong_single_description, null);
        View v = messageView.findViewById(R$id.textview);
        if (v instanceof TextView) {
            ((TextView) v).setText(R$string.emui30_keyguard_management_remove);
            this.mRemoveDialog = new Builder(context).setView(messageView).setPositiveButton(R$string.emui30_keyguard_remove, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    GlobalContext.getBackgroundHandler().post(new Runnable() {
                        public void run() {
                            BigPictureInfo info = MagazineWallpaper.getInst(MagazineControlView.this.mContext).removeCurrentWallpaper();
                            ClientHelper.getInstance().deletePicFormDB(MagazineControlView.this.mContext, info);
                            if (MagazineControlView.isBigPictureInfoValid(info)) {
                                HwLockScreenReporter.report(MagazineControlView.this.mContext, 111, "{remove: " + info.getPicUniqueName() + ",channel:" + info.getChannelId() + "}");
                                HwLockScreenReporter.reportAdEvent(MagazineControlView.this.mContext, info, EventType.REMOVE);
                                HwLockScreenReporter.statReport(MagazineControlView.this.mContext, 1005, "{picture:" + info.getPicName() + "}");
                            }
                            MagazineControlView.this.mHandler.post(MagazineControlView.this.mUpdateControlState);
                        }
                    });
                }
            }).setNegativeButton(17039360, null).create();
            LayoutParams layoutParams = this.mRemoveDialog.getWindow().getAttributes();
            layoutParams.y = getContext().getResources().getDimensionPixelSize(17104920) + getResources().getDimensionPixelSize(R$dimen.alert_dialog_navigation_bar_margin_top);
            this.mRemoveDialog.getWindow().setAttributes(layoutParams);
            this.mRemoveDialog.getWindow().setType(2009);
            this.mRemoveDialog.show();
            Button pButtin = this.mRemoveDialog.getButton(-1);
            if (pButtin != null) {
                pButtin.setTextColor(context.getResources().getColor(R$color.magazine_delete_textcolor));
            }
        }
    }

    private static boolean isBigPictureInfoValid(BigPictureInfo info) {
        if (info == null || info.getIdentityInfo() == null) {
            HwLog.w("MagazineControlView", "BigPicture Info is null or IdentityInfo is null");
            return false;
        } else if (!TextUtils.isEmpty(info.getPicUniqueName()) && !TextUtils.isEmpty(info.getTitle())) {
            return true;
        } else {
            HwLog.w("MagazineControlView", "PictureInfo not valide: picUniqueName is =" + info.getPicUniqueName() + ", title=" + info.getTitle());
            return false;
        }
    }

    private void respondSettings() {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        if (KeyguardCfg.isMagazineUpdateEnabled()) {
            intent.setClassName("com.android.keyguard", "com.huawei.keyguard.magazine.settings.SubscriptionActivity");
            intent.putExtra("updateAvailable", this.checkedNewVersion);
        } else {
            intent.setAction("com.android.keyguard.PHOTO_MANAGER");
            intent.putExtra("photoType", 1);
        }
        startActivity(intent, true);
    }

    public void startActivity(Intent intent, boolean doUnlock) {
        HwKeyguardPolicy.getInst().startActivity(intent, doUnlock);
    }

    private void respondSwitch() {
        boolean z;
        boolean z2 = false;
        Context context = getContext();
        boolean isAutoSwitchBefore = MagazineUtils.isAutoSwitchMagazine(context, true);
        this.mSwitchView.setImageResource(isAutoSwitchBefore ? R$drawable.ic_home_play : R$drawable.ic_home_pause);
        this.mSwitchView.setContentDescription(getContext().getText(isAutoSwitchBefore ? R$string.accessibility_magazine_info_switc_start : R$string.accessibility_magazine_info_switc_pause));
        if (isAutoSwitchBefore) {
            z = false;
        } else {
            z = true;
        }
        MagazineUtils.setAutoSwitchMagazine(context, z, "user manual switch");
        HwLog.d("MagazineControlView", "User reset SP_AUTO_SWITCH " + (!isAutoSwitchBefore ? "on" : "off"));
        BigPictureInfo info = MagazineWallpaper.getInst(this.mContext).getPictureInfo(0);
        if (!isAutoSwitchBefore) {
            HwLockScreenReporter.report(context, 109, BuildConfig.FLAVOR);
            KeyguardWallpaper.getInst(this.mContext).setUserCustomedWallpaper(this.mContext, false, false);
        } else if (isBigPictureInfoValid(info)) {
            HwLockScreenReporter.report(context, 110, "{auto-switch pause: " + info.getPicUniqueName() + ", channel: " + info.getTitle() + "}");
        }
        if (info != null) {
            Context context2 = this.mContext;
            StringBuilder append = new StringBuilder().append("{picture:").append(info.getPicName()).append(", status:");
            if (!isAutoSwitchBefore) {
                z2 = true;
            }
            HwLockScreenReporter.statReport(context2, 1007, append.append(z2).append("}").toString());
        }
    }

    private void respondShare() {
        Intent intent = new Intent("com.huawei.android.internal.app.RESOLVER");
        intent.setPackage("com.huawei.android.internal.app");
        if (KeyguardUtils.getTargetReceiverSize(this.mContext, intent) == 1) {
            BigPicture bigPic = MagazineWallpaper.getInst(this.mContext).getWallPaper(0);
            final BigPictureInfo bigPictureInfo = bigPic == null ? null : bigPic.getBigPictureInfo();
            if (this.mPicShareTask != null || bigPictureInfo == null) {
                HwLog.i("MagazineControlView", "skip share as last task not finish or no info." + this.mPicShareTask);
                return;
            }
            this.mPicShareTask = new AsyncTask<Void, Void, String>() {
                protected void onPostExecute(String result) {
                    MagazineControlView.this.shareMagazineImgFile(result);
                    MagazineControlView.this.mPicShareTask = null;
                }

                protected String doInBackground(Void... params) {
                    return MagazineControlView.this.getWaterMarkedPicture(bigPictureInfo);
                }
            }.execute(new Void[0]);
            HwLockScreenReporter.reportAdEvent(this.mContext, bigPictureInfo, EventType.SHARE);
            HwLockScreenReporter.statReport(this.mContext, 1006, "{picture:" + bigPictureInfo.getPicName() + "}");
        } else {
            HwLog.e("MagazineControlView", "shared fail with hw resolver");
        }
    }

    private String getBigPictureInfoPath(BigPictureInfo info) {
        String picPath = info.getPicPath();
        if (HwUnlockUtils.isLandscape(this.mContext) && !TextUtils.isEmpty(picPath)) {
            String picPathLand = MagazineUtils.getLandPicName(picPath);
            if (new File(picPathLand).exists()) {
                return picPathLand;
            }
        }
        return picPath;
    }

    private String getWaterMarkedPicture(BigPictureInfo info) {
        String picPath = getBigPictureInfoPath(info);
        String waterMarkFilePath = MagazineWaterMark.getWaterMarkFileSavePath(picPath);
        if (waterMarkFilePath == null) {
            return picPath;
        }
        MagazineWaterMark.deleteNotUsedWaterMarkFiles();
        String des = info.getContent();
        if (!TextUtils.isEmpty(des)) {
            if (!(info.getDescriptionInfo() == null || info.getDescriptionInfo().isWorksMagazineUnlock())) {
                des = des + getResources().getString(R$string.hw_magazine_unlock_logo);
            }
            try {
                if (MagazineWaterMark.generateWatermarkBitmapFile(this.mContext, picPath, des, waterMarkFilePath)) {
                    picPath = waterMarkFilePath;
                }
            } catch (Exception e) {
                HwLog.e("MagazineControlView", "generateWatermarkBitmapFile error");
            } catch (OutOfMemoryError e2) {
                HwLog.e("MagazineControlView", "generateWatermarkBitmapFile OutOfMemoryError");
            }
        }
        return picPath;
    }

    private void shareMagazineImgFile(String filePath) {
        Uri uri = Uri.parse("file://" + filePath);
        Intent target = new Intent("android.intent.action.SEND");
        target.setType("image/*");
        target.putExtra("android.intent.extra.TITLE", getContext().getResources().getString(R$string.magazine_info_share));
        target.addFlags(268435456);
        target.putExtra("android.intent.extra.STREAM", uri);
        Intent it = new Intent();
        it.putExtra("android.intent.extra.INTENT", target);
        it.addFlags(268435456);
        it.setAction("com.huawei.android.internal.app.RESOLVER");
        getContext().sendBroadcast(it, "com.huawei.hwresolver.resolverReceiver");
    }

    public void updateMagazineInfo(BigPictureInfo info) {
        if (info == null) {
            updateMagazineInfo(BuildConfig.FLAVOR, BuildConfig.FLAVOR, BuildConfig.FLAVOR, false);
            return;
        }
        if (info.getDescriptionInfo() == null) {
            updateMagazineInfo(info.getTitle(), info.getContent(), BuildConfig.FLAVOR, false);
        }
        updateMagazineInfo(info.getTitle(), info.getContent(), info.getDescriptionInfo().getWorksDes(), info.getDescriptionInfo().getLinkVisible());
        if (info.getGalleryInfo() != null && OsUtils.isOwner()) {
            updateMagazineFavoriteStatus(info.getFavoriteInfo(), info.getIsCustom());
        }
    }

    private void updateMagazineInfo(String title, String detail, String worksDes, boolean linkVisible) {
        if (this.mMagazineDetail == null) {
            HwLog.w("MagazineControlView", "updateMagazineInfo skipped.");
            return;
        }
        if (detail == null) {
            detail = BuildConfig.FLAVOR;
        }
        this.mTitleName = title;
        this.mMagazineDetail.setVisibility(isDetailEmpty(detail) ? 8 : 0);
        if (this.mMagazineDetail instanceof LocalLinkTextView) {
            this.mMagazineDetail.setDescription(detail, worksDes, linkVisible);
        } else {
            this.mMagazineDetail.setText(detail);
        }
        updateSwitchView();
        updateContentDescription();
        resetViewForSubUsers();
    }

    private void updateMagazineFavoriteStatus(boolean isFavorite, boolean isCustom) {
        if (this.mMagazineFavorite != null && KeyguardCfg.isMagazineUpdateEnabled()) {
            this.mFavoriteStatus = isFavorite;
            if (this.mFavoriteStatus) {
                this.mMagazineFavorite.setImageResource(R$drawable.ic_public_favor);
            } else {
                this.mMagazineFavorite.setImageResource(R$drawable.ic_public_unfavor);
            }
            this.mMagazineFavorite.setEnabled(true);
            if (OsUtils.isOwner()) {
                this.mMagazineFavorite.setVisibility(0);
            }
            setViewVisibility(this.mMagazineEmpty, 8);
        }
    }

    public boolean isDetailEmpty(String detail) {
        boolean empty = TextUtils.isEmpty(detail);
        if (empty) {
            return empty;
        }
        byte[] bytes = detail.getBytes(Charset.defaultCharset());
        if (bytes.length != 1 || bytes[0] != (byte) 0) {
            return empty;
        }
        HwLog.d("MagazineControlView", "detail is empty by 0 string");
        return true;
    }

    private void resetViewForSubUsers() {
        if (OsUtils.isOwner()) {
            setViewVisibility(this.mRemoveView, 0);
            setViewVisibility(this.mShareView, 0);
            setViewVisibility(this.mSettingsView, 0);
            setViewVisibility(this.mMagazineFavorite, 0);
            if (!(this.mMagazineFavorite == null || this.mMagazineFavorite.isEnabled() || !KeyguardCfg.isMagazineUpdateEnabled())) {
                this.mMagazineFavorite.setEnabled(true);
            }
            setViewVisibility(this.mSwitchView, 0);
            return;
        }
        setViewVisibility(this.mRemoveView, 8);
        setViewVisibility(this.mShareView, 8);
        setViewVisibility(this.mSettingsView, 8);
        setViewVisibility(this.mMagazineEmpty, 8);
        setViewVisibility(this.mMagazineFavorite, 8);
        setViewVisibility(this.mSwitchView, 8);
    }

    private void setViewVisibility(View textView, int visibleStatus) {
        if (textView != null) {
            textView.setVisibility(visibleStatus);
        }
    }

    private boolean needShowToastForFavorite() {
        SharedPreferences sp = this.mContext.getSharedPreferences("magazine_preferences", 0);
        int toastTime = sp.getInt("show_favorite_toast_time", 0);
        if (toastTime >= 3) {
            return false;
        }
        toastTime++;
        Editor editor = sp.edit();
        editor.putInt("show_favorite_toast_time", toastTime);
        editor.commit();
        return true;
    }

    private void respondFavorite() {
        updateFavoritesStatus(true);
        if (this.mFavoriteStatus && needShowToastForFavorite()) {
            KeyguardToast kt = KeyguardToast.makeText(this.mContext, R$string.emui30_keyguard_cover_like_toast, 0);
            if (kt != null) {
                kt.show();
            }
        }
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                BigPictureInfo info = MagazineWallpaper.getInst(MagazineControlView.this.mContext).getPictureInfo(0);
                if (info != null) {
                    int reporterEventId;
                    info.setFavoriteInfo(MagazineControlView.this.mFavoriteStatus);
                    ClientHelper.getInstance().updateFavoriteStatusToPicDB(MagazineControlView.this.mContext, info);
                    if (MagazineControlView.this.mFavoriteStatus) {
                        reporterEventId = 132;
                    } else {
                        reporterEventId = 133;
                    }
                    HwLockScreenReporter.report(MagazineControlView.this.mContext, reporterEventId, "{title_name:" + MagazineControlView.this.mTitleName + "}");
                    if (MagazineControlView.this.mFavoriteStatus) {
                        HwLockScreenReporter.reportAdEvent(MagazineControlView.this.mContext, info, EventType.FAVORITE);
                    }
                    HwLockScreenReporter.statReport(MagazineControlView.this.mContext, 1008, "{picture:" + info.getPicName() + ", status:" + MagazineControlView.this.mFavoriteStatus + ", amount:" + ClientHelper.getInstance().queryFavoritePictureAmount(MagazineControlView.this.mContext) + "}");
                }
            }
        });
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mRemoveDialog != null && this.mRemoveDialog.isShowing()) {
            this.mRemoveDialog.dismiss();
        }
        EventCenter.getInst().stopListenContent(this);
    }

    void setButtonsClickable(boolean lift) {
        this.mChildButtonClickable = lift;
    }
}
