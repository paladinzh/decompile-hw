package com.android.gallery3d.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareMediaItem;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.ShareExecutor;
import com.android.gallery3d.ui.ShareExecutor.ShareExecutorListener;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ImageVideoTranser;
import com.android.gallery3d.util.InstantShareUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.share.Delegate;
import com.huawei.gallery.share.HwResolverView;
import com.huawei.gallery.share.HwResolverView.DisplayResolveInfo;
import com.huawei.gallery.share.HwResolverView.GridViewClickListener;
import com.huawei.gallery.video.ShareClickListener;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntentChooser implements Delegate, ShareClickListener, GridViewClickListener, OnDismissListener {
    private static final String TAG = IntentChooser.class.getSimpleName();
    private static final BroadcastReceiver mCheckKeyguardOccludedSetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() != 0) {
                try {
                    WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
                    return;
                } catch (Exception e) {
                    GalleryLog.w(IntentChooser.TAG, "dismiss keyguard fail", e);
                    return;
                }
            }
            GalleryLog.w(IntentChooser.TAG, "UNLOCKED_KEYGUARD canceled");
        }
    };
    private static Intent mShareBroadcastIntent = new Intent("com.huawei.android.internal.app.RESOLVER").setPackage("com.huawei.android.internal.app");
    private static IntentChooser sIntentHandler;
    private static final Comparator<IShareItem> sKeyComparator = new KeyComparator();
    private Activity mActivity;
    private List<IShareItem> mAddOnItems;
    private AppsListOnClickedlistener mAppsListOnclickedlistener;
    private boolean mAwaysUseOption;
    private AlertDialog mDialog;
    private boolean mDialogCreating;
    private Handler mHandler;
    private boolean mHasReceiver;
    private Intent mIntent;
    private IntentChooserDialogClickListener mIntentChooserDialogClickListener;
    private boolean mIsActive;
    private boolean mIsSecureAlbum;
    private KeyguardManager mKeyguardManager;
    private ActionBarStateBase mMode;
    private Runnable mShareActionEnableRunnable;
    private MMShareInterceptor mShareCallback;
    private IntentFilter mShareCallbackFilter;
    private BroadcastReceiver mShareCallbackReceiver;
    private int mShareType;
    VIConfirmListener mShareUserVIListener;
    private boolean mUseBroadcast;

    public interface AppsListOnClickedlistener {
        boolean onIntentChooserDialogAppsItemClicked();
    }

    public interface IShareItem {
        ComponentName getComponent();

        Drawable getIcon();

        int getKey();

        String getLabel();

        String[] getSupportActions();

        void onClicked(Intent intent);
    }

    public interface IntentChooserDialogClickListener {
        void onClickCancel();

        void onClickItem();
    }

    private static class KeyComparator implements Comparator<IShareItem>, Serializable {
        private KeyComparator() {
        }

        public int compare(IShareItem item1, IShareItem item2) {
            return Utils.compare((long) item1.getKey(), (long) item2.getKey());
        }
    }

    private class ShareReceiver extends BroadcastReceiver {
        private ShareReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            IntentChooser.this.onReceiveShareResult(500, -1, intent);
        }
    }

    private class VIConfirmListener {
        private VIConfirmListener() {
        }
    }

    public void setGridListOnclickedlistener(AppsListOnClickedlistener listener) {
        this.mAppsListOnclickedlistener = listener;
    }

    public void setIntentChooserDialogClickListener(IntentChooserDialogClickListener listener) {
        this.mIntentChooserDialogClickListener = listener;
    }

    public IntentChooser(Activity context) {
        this(context, false);
    }

    public IntentChooser(Activity context, boolean isSecureAlbum) {
        this.mDialogCreating = false;
        this.mAwaysUseOption = false;
        this.mIsActive = false;
        this.mAddOnItems = new ArrayList();
        this.mShareCallbackFilter = new IntentFilter("com.android.gallery.ACTION_SHARE_CALLBACK");
        this.mShareCallbackReceiver = new ShareReceiver();
        this.mUseBroadcast = false;
        this.mHasReceiver = false;
        this.mIsSecureAlbum = false;
        this.mShareActionEnableRunnable = new Runnable() {
            public void run() {
                IntentChooser.this.disableShareAction(false);
                IntentChooser.this.mMode = null;
            }
        };
        this.mShareUserVIListener = new VIConfirmListener();
        TraceController.traceBegin("IntentChooser");
        this.mActivity = context;
        this.mKeyguardManager = (KeyguardManager) this.mActivity.getSystemService("keyguard");
        this.mIsSecureAlbum = isSecureAlbum;
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (IntentChooser.sIntentHandler == IntentChooser.this) {
                    GalleryLog.d(IntentChooser.TAG, "received boradcast handle it ");
                    IntentChooser.sIntentHandler = null;
                    Intent intent = msg.obj;
                    ComponentName cm = intent.getComponent();
                    intent.setFlags(intent.getFlags() & -268435457);
                    if (IntentChooser.this.mIsSecureAlbum) {
                        intent.addFlags(268484608);
                    }
                    GalleryLog.d(IntentChooser.TAG, "component received : " + cm);
                    intent.removeExtra("android.intent.extra.INITIAL_INTENTS");
                    intent.removeExtra("android.intent.extra.TITLE");
                    if (cm == null) {
                        GalleryLog.d(IntentChooser.TAG, "component null, start by default intent.");
                        GalleryUtils.startActivityCatchSecurityEx(IntentChooser.this.mActivity, intent);
                        return;
                    }
                    if (msg.arg1 == 1) {
                        ReportToBigData.report(86, String.format("{ShareTo:%s}", new Object[]{cm.getPackageName()}));
                    } else if (msg.arg1 == 2) {
                        ReportToBigData.report(141, String.format("{ShareTo:%s}", new Object[]{cm.getPackageName()}));
                    } else {
                        GalleryLog.d(IntentChooser.TAG, "unknowPhoto");
                    }
                    String action = intent.getAction();
                    if (!TextUtils.isEmpty(action) && ("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action))) {
                        IntentChooser.this.reportDataForShareItemChosed(cm);
                    }
                    for (IShareItem item : IntentChooser.this.mAddOnItems) {
                        if (cm.equals(item.getComponent())) {
                            item.onClicked(intent);
                            return;
                        }
                    }
                    if (IntentChooser.this.mShareCallback == null || !IntentChooser.this.mShareCallback.onShare(intent)) {
                        GalleryUtils.startActivityCatchSecurityEx(IntentChooser.this.mActivity, intent);
                        return;
                    }
                    return;
                }
                GalleryLog.d(IntentChooser.TAG, "received boradcast shouldn't handle by me. ");
            }
        };
        TraceController.traceEnd();
    }

    public void resume() {
        if (this.mUseBroadcast) {
            GalleryLog.d(TAG, "[resume] registerReceiver");
            this.mActivity.registerReceiver(this.mShareCallbackReceiver, this.mShareCallbackFilter, "com.huawei.gallery.permission.SHARE_CALLBACK", null);
            this.mHasReceiver = true;
        }
    }

    public void pause() {
        if (this.mShareCallback != null) {
            this.mShareCallback.onPause();
        }
        if (this.mUseBroadcast && this.mHasReceiver) {
            GalleryLog.d(TAG, "[pause] unregisterReceiver");
            this.mActivity.unregisterReceiver(this.mShareCallbackReceiver);
            this.mHasReceiver = false;
        }
    }

    public void onResult() {
        if (this.mShareCallback != null) {
            this.mShareCallback.onResult();
        }
    }

    public void setAwaysUseOption(boolean useOption) {
        this.mAwaysUseOption = useOption;
    }

    public boolean addShareItem(IShareItem shareItem) {
        if (shareItem == null || this.mAddOnItems.contains(shareItem)) {
            return false;
        }
        return this.mAddOnItems.add(shareItem);
    }

    public void removeShareItem(IShareItem shareItem) {
        if (!this.mAddOnItems.isEmpty() && this.mAddOnItems.contains(shareItem)) {
            this.mAddOnItems.remove(shareItem);
        }
    }

    public void sendDismissKeyguardBroadcast() {
        Intent unlockKeyguardIntent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD");
        if (this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardSecure()) {
            try {
                WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
                return;
            } catch (Exception e) {
                GalleryLog.w(TAG, "dismissKeyguard exception", e);
                return;
            }
        }
        this.mActivity.sendOrderedBroadcast(unlockKeyguardIntent, "android.permission.DISABLE_KEYGUARD", mCheckKeyguardOccludedSetReceiver, null, -1, null, null);
    }

    private Intent[] getAddOnIntents(Intent intent) {
        List<IShareItem> addOn = new ArrayList(this.mAddOnItems);
        Collections.sort(addOn, sKeyComparator);
        Intent[] intIntents = new Intent[addOn.size()];
        int index = 0;
        for (IShareItem item : addOn) {
            Intent addonIntent = new Intent(intent);
            addonIntent.setComponent(item.getComponent());
            String[] actions = item.getSupportActions();
            if (actions != null && actions.length > 0) {
                addonIntent.putExtra("support-actions", actions);
            }
            int index2 = index + 1;
            intIntents[index] = addonIntent;
            index = index2;
        }
        return intIntents;
    }

    private void useBroadcast(Intent intent) {
        Intent chooserIntent = new Intent();
        chooserIntent.setAction("android.intent.action.hwINSTANTSHARE");
        chooserIntent.putExtra("android.intent.extra.INTENT", intent);
        chooserIntent.addFlags(1);
        chooserIntent.putExtra("called-from-gallery", true);
        chooserIntent.putExtra("called-when-screen-locked", this.mIsSecureAlbum);
        ComponentName componentName = chooserIntent.resolveActivity(this.mActivity.getPackageManager());
        String action = intent.getAction();
        if (("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action)) && componentName != null) {
            sendDismissKeyguardBroadcast();
            String[] strArr = new String[]{"text/uri-list"};
            chooserIntent.setClipData(new ClipData(null, strArr, new Item(InstantShareUtils.INSTANTSHARE_QUERY_URI)));
            GalleryUtils.startActivityForResultCatchSecurityEx(this.mActivity, chooserIntent, 500);
            return;
        }
        Intent chooserIntent2 = new Intent(mShareBroadcastIntent);
        chooserIntent2.putExtra("android.intent.extra.INTENT", intent);
        chooserIntent2.addFlags(268435457);
        chooserIntent2.putExtra("called-from-gallery", true);
        chooserIntent.putExtra("called-when-screen-locked", this.mIsSecureAlbum);
        this.mActivity.sendBroadcast(chooserIntent2, "com.huawei.hwresolver.resolverReceiver");
    }

    private boolean jumpOverShowDialog() {
        return (this.mDialog != null && this.mDialog.isShowing()) || this.mDialogCreating || !this.mIsActive;
    }

    private void showDialog(Intent intent, int title) {
        if (!this.mUseBroadcast) {
            boolean z;
            List<ResolveInfo> mResolveInfos = this.mActivity.getPackageManager().queryBroadcastReceivers(mShareBroadcastIntent, 0);
            if (mResolveInfos == null || mResolveInfos.isEmpty()) {
                z = false;
            } else {
                z = true;
            }
            this.mUseBroadcast = z;
            GalleryLog.d(TAG, "mUseBroadCast " + this.mUseBroadcast);
            if (this.mUseBroadcast && !this.mHasReceiver) {
                GalleryLog.d(TAG, "[showDialog] registerReceiver");
                this.mActivity.registerReceiver(this.mShareCallbackReceiver, this.mShareCallbackFilter, "com.huawei.gallery.permission.SHARE_CALLBACK", null);
                this.mHasReceiver = true;
            }
        }
        if (this.mUseBroadcast && this.mHasReceiver) {
            intent.putExtra("android.intent.extra.TITLE", this.mActivity.getResources().getString(title));
            intent.addFlags(268435456);
            if (title == R.string.share) {
                intent.putExtra("android.intent.extra.INITIAL_INTENTS", getAddOnIntents(intent));
            }
            sIntentHandler = this;
            useBroadcast(intent);
        } else if (!jumpOverShowDialog()) {
            try {
                this.mDialogCreating = true;
                ContextThemeWrapper context = GalleryUtils.getHwThemeContext(this.mActivity, "androidhwext:style/Theme.Emui.Dialog");
                if (context == null) {
                    GalleryLog.w(TAG, "getHwThemeContext() is null");
                    return;
                }
                HwResolverView view = (HwResolverView) View.inflate(context, R.layout.resolver_grid_emui, null);
                if (view == null) {
                    GalleryLog.e(TAG, "error share view is null when startShareDialog");
                    this.mDialogCreating = false;
                    return;
                }
                this.mIntent = intent;
                this.mShareType = title;
                this.mDialog = new Builder(context).create();
                view.fillView(this);
                view.setGridViewClickListener(this);
                view.setShareClickListener(this);
                this.mDialog.setTitle(title);
                int padding = this.mActivity.getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
                this.mDialog.setView(view, padding, 0, padding, 0);
                this.mDialog.setOnDismissListener(this);
                this.mDialog.show();
                this.mDialogCreating = false;
            } finally {
                this.mDialogCreating = false;
            }
        }
    }

    public void hideIfShowing() {
        this.mIsActive = false;
        if (this.mDialog != null && this.mDialog.isShowing()) {
            GalleryUtils.dismissDialogSafely(this.mDialog, null);
        }
    }

    private void disableShareAction(boolean disable) {
        if (this.mMode != null) {
            this.mMode.setActionEnable(!disable, Action.ACTION_ID_SHARE);
        }
    }

    private String shareExecutorCreateImageDescription(MediaObject mediaObject) {
        String filePath = ((MediaItem) mediaObject).getFilePath();
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(filePath);
        } catch (FileNotFoundException e) {
            GalleryLog.w(TAG, "Could not find file to read exif: " + filePath + "." + e.getMessage());
        } catch (Throwable t) {
            GalleryLog.w(TAG, "Could not read exif from file: " + filePath + "." + t.getMessage());
        }
        return exif.getTagStringValue(ExifInterface.TAG_IMAGE_DESCRIPTION);
    }

    private void shareExecutorOnProcessDone(GalleryContext context, ArrayList<Uri> shareUris, ArrayList<String> mimeTypeList, ArrayList<Integer> orientationList, Path mMediaSetPath, boolean hasFoundFoodImage, boolean hasFoundMakeUpImage, String excludeAlbumID, boolean isPhotoShare, Intent shareIntent, int type) {
        int size = shareUris.size();
        if (size == 0) {
            ContextedUtils.showToastQuickly(context.getActivityContext(), (int) R.string.drm_cannot_share, 0);
            return;
        }
        String mimeType = MenuExecutor.getMimeType(type);
        shareIntent.removeExtra("KEY_PATH_ARRAY");
        if (size > 1) {
            shareIntent.setAction("android.intent.action.SEND_MULTIPLE").setType(mimeType);
            shareIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", shareUris);
        } else {
            shareIntent.setAction("android.intent.action.SEND").setType(mimeType);
            shareIntent.putExtra("android.intent.extra.STREAM", (Parcelable) shareUris.get(0));
        }
        shareIntent.putStringArrayListExtra("KEY_ITEM_MIME_TYPE_LIST", mimeTypeList);
        shareIntent.putIntegerArrayListExtra("KEY_ITEM_ORIENTATION_LIST", orientationList);
        if (mMediaSetPath != null) {
            shareIntent.putExtra("KEY_MEDIA_SET_PATH", mMediaSetPath.toString());
        } else {
            shareIntent.removeExtra("KEY_MEDIA_SET_PATH");
        }
        shareIntent.putExtra("food", hasFoundFoodImage);
        shareIntent.putExtra("makeup", hasFoundMakeUpImage);
        if (isPhotoShare) {
            shareIntent.putExtra("exclude-path", excludeAlbumID);
        }
        showDialog(shareIntent, R.string.share);
        this.mHandler.postDelayed(this.mShareActionEnableRunnable, 500);
    }

    private void handleIntentForVI(final GalleryContext context, final boolean shouldTransToVideo, MenuExecutor executor, final Path mediaSetPath, ArrayList<Path> items) {
        this.mHandler.removeCallbacks(this.mShareActionEnableRunnable);
        disableShareAction(ShareExecutor.needShowWaitDialogAndDisableAction(items.size()));
        ShareExecutor.convertShareItems(items, context, executor, new ShareExecutorListener() {
            private String excludeAlbumID = null;
            private boolean hasFoundFoodImage = false;
            private boolean hasFoundMakeUpImage = false;
            private boolean isPhotoShare = false;
            private Path mMediaSetPath = mediaSetPath;
            private ArrayList<String> mimeTypeList = new ArrayList();
            private ArrayList<Integer> orientationList = new ArrayList();
            Intent shareIntent = new Intent();
            private int type = 0;

            public boolean shouldConvertVI() {
                return shouldTransToVideo;
            }

            public void onProgress(MediaObject mediaObject) {
                if (mediaObject.getMediaType() == 2 && (mediaObject instanceof MediaItem)) {
                    String exifTagImageDescription = IntentChooser.this.shareExecutorCreateImageDescription(mediaObject);
                    if (exifTagImageDescription != null) {
                        boolean z;
                        if (!this.hasFoundFoodImage) {
                            GalleryLog.d(IntentChooser.TAG, "Nice Food handleIntentForVI():exifTagImageDescription=" + exifTagImageDescription);
                            if (exifTagImageDescription.indexOf("nfd") >= 0) {
                                z = true;
                            } else {
                                z = false;
                            }
                            this.hasFoundFoodImage = z;
                        }
                        if (!this.hasFoundMakeUpImage) {
                            GalleryLog.d(IntentChooser.TAG, "MakeUp handleIntentForVI():exifTagImageDescription=" + exifTagImageDescription);
                            if (exifTagImageDescription.indexOf("mup") >= 0) {
                                z = true;
                            } else {
                                z = false;
                            }
                            this.hasFoundMakeUpImage = z;
                        }
                    }
                }
                if (shouldTransToVideo(mediaObject)) {
                    this.type |= 4;
                    updateMimeTypeList(mediaObject, this.mimeTypeList, MenuExecutor.getMimeType(4));
                    updateOrientationList(mediaObject, this.orientationList, Integer.valueOf(0));
                    ReportToBigData.report(52, "");
                    return;
                }
                if (!this.isPhotoShare && (mediaObject instanceof PhotoShareMediaItem)) {
                    this.isPhotoShare = true;
                    this.excludeAlbumID = mediaObject.getPath().getParent().getSuffix();
                }
                this.type |= mediaObject.getMediaType();
                updateMimeTypeList(mediaObject, this.mimeTypeList, null);
                updateOrientationList(mediaObject, this.orientationList, null);
            }

            private boolean shouldTransToVideo(MediaObject mediaObject) {
                return shouldTransToVideo ? ImageVideoTranser.isItemSupportTransVer(mediaObject) : false;
            }

            public void onProcessDone(ArrayList<Uri> shareUris) {
                IntentChooser.this.shareExecutorOnProcessDone(context, shareUris, this.mimeTypeList, this.orientationList, this.mMediaSetPath, this.hasFoundFoodImage, this.hasFoundMakeUpImage, this.excludeAlbumID, this.isPhotoShare, this.shareIntent, this.type);
            }

            private void updateMimeTypeList(MediaObject mediaObject, ArrayList<String> mimeTypeList, String mimeType) {
                if (mimeTypeList != null) {
                    if (mimeType == null || mimeType.isEmpty()) {
                        mimeTypeList.add(MenuExecutor.getMimeType(mediaObject.getMediaType()));
                    } else {
                        mimeTypeList.add(mimeType);
                    }
                }
            }

            private void updateOrientationList(MediaObject mediaObject, ArrayList<Integer> orientationList, Integer orientation) {
                if (orientationList != null) {
                    if (orientation != null) {
                        orientationList.add(orientation);
                    } else if (mediaObject instanceof MediaItem) {
                        orientationList.add(Integer.valueOf(((MediaItem) mediaObject).getRotation()));
                    } else {
                        orientationList.add(Integer.valueOf(0));
                    }
                }
            }
        });
    }

    public void share(GalleryContext context, ActionBarStateBase mode, MenuExecutor executor, Path mediaSetPath, ArrayList<Path> items) {
        this.mIsActive = true;
        this.mMode = mode;
        if (checkMediaItemSharable(items, context)) {
            if (this.mShareCallback == null) {
                this.mShareCallback = new MMShareInterceptor(this.mActivity);
            }
            this.mShareCallback.clearCachedFile();
            handleIntentForVI(context, false, executor, mediaSetPath, items);
            return;
        }
        ContextedUtils.showToastQuickly(context.getActivityContext(), (int) R.string.not_support_share, 0);
    }

    private boolean checkMediaItemSharable(ArrayList<Path> pathArray, GalleryContext activity) {
        if (!(pathArray == null || pathArray.isEmpty())) {
            DataManager manager = activity.getDataManager();
            for (Path path : pathArray) {
                MediaObject item = manager.getMediaObject(path);
                if (item != null && (item instanceof MediaItem) && !((MediaItem) item).canShare()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void startDialogWithChooser(Intent intent, int title) {
        this.mIsActive = true;
        showDialog(intent, title);
    }

    public ArrayList<DisplayResolveInfo> getGalleryShareItem() {
        ArrayList<DisplayResolveInfo> result = new ArrayList();
        if (this.mAddOnItems == null || this.mShareType != R.string.share) {
            return result;
        }
        Collections.sort(this.mAddOnItems, sKeyComparator);
        for (IShareItem item : this.mAddOnItems) {
            result.add(new DisplayResolveInfo(item));
        }
        return result;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    public void itemClicked() {
        if (this.mAppsListOnclickedlistener != null) {
            this.mAppsListOnclickedlistener.onIntentChooserDialogAppsItemClicked();
        }
        if (this.mIntentChooserDialogClickListener != null) {
            this.mIntentChooserDialogClickListener.onClickItem();
            this.mIntentChooserDialogClickListener = null;
        }
    }

    public void onClickCancel() {
        if (this.mIntentChooserDialogClickListener != null) {
            this.mIntentChooserDialogClickListener.onClickCancel();
            this.mIntentChooserDialogClickListener = null;
        }
    }

    public boolean onShareItemClicked(Intent toBeSend) {
        if (this.mShareCallback != null) {
            return this.mShareCallback.onShare(toBeSend);
        }
        return false;
    }

    public boolean alwaysUseOption() {
        return this.mAwaysUseOption;
    }

    public void onDismiss(DialogInterface dialog) {
        this.mAwaysUseOption = false;
        this.mIsActive = false;
    }

    private void reportDataForShareItemChosed(ComponentName cm) {
        if (cm != null) {
            String packageName = cm.getPackageName();
            if (HicloudAccountManager.PACKAGE_NAME.equalsIgnoreCase(packageName)) {
                packageName = "com.huawei.gallery.photoshare.ui.ShareToCloudAlbumActivity".equalsIgnoreCase(cm.getClassName()) ? "CloudAlbum" : "FreeShare";
            }
            ReportToBigData.report(OfflineMapStatus.EXCEPTION_SDCARD, String.format("{ShareTo:%s}", new Object[]{packageName}));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceiveShareResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 500 && resultCode == -1 && intent != null) {
            Intent target = (Intent) intent.getParcelableExtra("target-intent");
            GalleryLog.d(TAG, " target(target-intent) received : " + intent);
            itemClicked();
            if (target != null) {
                int photoType;
                if (target.getBooleanExtra("food", false)) {
                    photoType = 1;
                } else if (target.getBooleanExtra("makeup", false)) {
                    photoType = 2;
                } else {
                    photoType = 0;
                }
                this.mHandler.obtainMessage(1, photoType, 1, target).sendToTarget();
                sendDismissKeyguardBroadcast();
            }
        }
    }
}
