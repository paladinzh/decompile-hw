package com.huawei.gallery.extfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.common.ext.util.FyuseUtils.FyuseContainerVersion;
import com.fyusion.sdk.ext.shareinterface.FyuseShareInterface;
import com.fyusion.sdk.ext.shareinterface.ShareInterfaceListener;
import com.fyusion.sdk.share.CallbackManager;
import com.fyusion.sdk.share.CallbackManager.Factory;
import com.fyusion.sdk.share.FyuseShare;
import com.fyusion.sdk.share.FyuseShareParameters;
import com.fyusion.sdk.share.ShareListener;
import com.huawei.gallery.panorama.ServiceSelectClickListener;
import com.huawei.gallery.panorama.ShareObject;
import com.huawei.gallery.panorama.ShareService;
import com.huawei.gallery.panorama.ShareServicesAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FyuseManager {
    private static FyuseManager mFyuseManager;
    private Activity mActivity;
    private OnClickListener mAllowClickListener = new AllowClickListener();
    private CallbackManager mCallbackManager = Factory.create();
    private OnClickListener mCancelClickListener = new CancelClickListener();
    private String mCurfileName;
    private String mCurfilePath;
    private AlertDialog mPermissionDialog = null;
    private SharedPreferences mPreference;
    private ShareObject mSelectedShareObject;
    private AlertDialog mShareDialog = null;
    private ShareListener mShareListener = new FyuseShareListener();

    private class AllowClickListener implements OnClickListener {
        private AllowClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            FyuseManager.this.closePermissonDialog((Dialog) dialog, true);
        }
    }

    private class CancelClickListener implements OnClickListener {
        private CancelClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            FyuseManager.this.closePermissonDialog((Dialog) dialog, false);
        }
    }

    private class FyuseShareListener implements ShareListener {
        private FyuseShareListener() {
        }

        public void onError(Exception fse) {
            GalleryLog.e("FyuseManager", "sharFyuseFile ShareListener onError = " + fse.getMessage());
            UploadActivity.finishActivity();
        }

        public void onProgress(int progress) {
        }

        public void onSuccess(String fyuseID) {
        }

        public void onSuccess(String fyuseId, Bitmap thumbnail) {
            UploadActivity.getInstance().finish();
            FyuseManager.this.shareSuccess(fyuseId);
        }

        public void onSuccess(String fyuseId, String thumbUrl) {
        }
    }

    private FyuseManager() {
    }

    public static synchronized FyuseManager getInstance() {
        FyuseManager fyuseManager;
        synchronized (FyuseManager.class) {
            if (mFyuseManager == null) {
                mFyuseManager = new FyuseManager();
            }
            fyuseManager = mFyuseManager;
        }
        return fyuseManager;
    }

    public boolean startViewFyuseFile(Context context, MediaItem item) {
        if (!isSupport3DPanoramaAPKOperation(item)) {
            return false;
        }
        GalleryLog.d("FyuseManager", "startViewFyuseFile with apk");
        return FyuseFile.startViewFyuseFile(context, item.getFilePath());
    }

    public boolean startEditFyuseFile(Context context, MediaItem item) {
        if (!isSupport3DPanoramaAPKOperation(item)) {
            return false;
        }
        GalleryLog.d("FyuseManager", "startEditFyuseFile with apk");
        return FyuseFile.startEditFyuseFile(context, item.getFilePath());
    }

    public boolean startShareFyuseFile(Activity activity, MediaItem item, String mode) {
        if (activity == null || item == null) {
            GalleryLog.d("FyuseManager", "startShareFyuseFile activity is " + activity + ", item is " + item);
            return false;
        } else if (isSupport3DPanoramaAPKOperation(item)) {
            GalleryLog.d("FyuseManager", "startShareFyuseFile with apk");
            return FyuseFile.startShareFyuseFile(activity, item.getFilePath(), mode);
        } else if (item.getSpecialFileType() != 20 || !isFyuseFile(item.getFilePath())) {
            return false;
        } else {
            this.mActivity = activity;
            this.mCurfilePath = item.getFilePath();
            this.mCurfileName = item.getName();
            this.mSelectedShareObject = new ShareObject();
            showShareDialog();
            this.mPreference = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
            return true;
        }
    }

    private boolean isSupport3DPanoramaAPKOperation(MediaItem item) {
        if (FyuseFile.isSupport3DPanoramaAPK() && item != null && item.getSpecialFileType() == 11) {
            return true;
        }
        return false;
    }

    private View createContentView() {
        if (this.mActivity == null) {
            return null;
        }
        View contentView = LayoutInflater.from(this.mActivity).inflate(R.layout.share_select_services, null);
        inflateSelectServices(contentView);
        return contentView;
    }

    private void showShareDialog() {
        if (this.mActivity != null) {
            this.mShareDialog = new Builder(this.mActivity).create();
            this.mShareDialog.setView(createContentView());
            this.mShareDialog.show();
        }
    }

    private List<ShareService> initShareServices() {
        if (this.mActivity == null) {
            return null;
        }
        List<ShareService> shareServices = new ArrayList();
        shareServices.add(new ShareService("fyuse", "fyuse", "Fyuse", ContextCompat.getDrawable(this.mActivity, R.drawable.ic_fyuse_logo)));
        Intent share = new Intent("android.intent.action.SEND");
        share.setType("text/plain");
        List<ResolveInfo> resInfo = this.mActivity.getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                try {
                    String strpackagename = info.activityInfo.packageName.toLowerCase();
                    share.setPackage(info.activityInfo.packageName);
                    shareServices.add(new ShareService(info.activityInfo.name, info.activityInfo.packageName, info.loadLabel(this.mActivity.getPackageManager()).toString(), info.loadIcon(this.mActivity.getPackageManager())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return shareServices;
    }

    private void onClickShare(View selectServices, ShareService shareService, boolean bNeedShare) {
        selectServices.setVisibility(4);
        if (this.mShareDialog != null && this.mShareDialog.isShowing()) {
            this.mShareDialog.dismiss();
            this.mShareDialog = null;
        }
        if (bNeedShare) {
            if (this.mSelectedShareObject != null) {
                this.mSelectedShareObject.setShareService(shareService);
            }
            if (this.mPreference == null || this.mPreference.getBoolean("is_never_remind", false)) {
                Intent intent = new Intent();
                intent.setClass(this.mActivity, UploadActivity.class);
                this.mActivity.startActivity(intent);
                sharFyuseFile();
            } else {
                showPermissonDialog();
            }
            return;
        }
        this.mSelectedShareObject = null;
    }

    private void inflateSelectServices(View view) {
        List<ShareService> shareServices = initShareServices();
        GridView gridview = (GridView) view.findViewById(R.id.select_services_grid);
        View selectServicesClose = view.findViewById(R.id.select_services_close);
        final View selectServices = view.findViewById(R.id.select_services);
        selectServices.setVisibility(0);
        ServiceSelectClickListener serviceSelectClickListener = new ServiceSelectClickListener() {
            public void onSelected(ShareService shareService) {
                FyuseManager.this.onClickShare(selectServices, shareService, true);
            }
        };
        selectServicesClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FyuseManager.this.onClickShare(selectServices, null, false);
            }
        });
        gridview.setAdapter(new ShareServicesAdapter(this.mActivity, shareServices, serviceSelectClickListener));
    }

    private void sharFyuseFile() {
        try {
            FyuseShareParameters.Builder builder = new FyuseShareParameters.Builder();
            builder.set(FyuseShareParameters.SHARE_RESOLUTION_FULL, Boolean.TRUE);
            FyuseShare.init().parameters(builder.build()).share(Uri.parse(this.mCurfilePath)).withDescription(this.mCurfileName).registerCallback(this.mActivity, this.mCallbackManager).setShareListener(this.mShareListener).start();
        } catch (FyuseSDKException fse) {
            GalleryLog.e("FyuseManager", "sharFyuseFile FyuseSDKException");
            this.mShareListener.onError(fse);
        }
    }

    private void shareSuccess(String fyuseId) {
        if ("fyuse".equals(this.mSelectedShareObject.getShareService().name)) {
            sharetoFyuse();
        } else {
            sharetoOtherApp(fyuseId);
        }
    }

    private void sharetoFyuse() {
        try {
            FyuseShareInterface.build().with(this.mActivity).registerCallback(this.mCallbackManager, new ShareInterfaceListener() {
                public void onSuccess(String fyuseURL) {
                }

                public void onError(Exception exception) {
                }

                public void onUserCancel() {
                }
            }).setUri(Uri.parse(this.mCurfilePath)).displayInterface();
        } catch (FyuseSDKException e) {
            GalleryLog.e("FyuseManager", "sharetoFyuse FyuseSDKException");
        }
    }

    private void sharetoOtherApp(String fyuseId) {
        Intent i = new Intent("android.intent.action.SEND");
        i.setType("text/plain");
        ShareService shareService = this.mSelectedShareObject.getShareService();
        i.setComponent(new ComponentName(shareService.packageName, shareService.name));
        i.putExtra("android.intent.extra.TEXT", "https://fyu.se/v/" + fyuseId);
        this.mActivity.startActivity(Intent.createChooser(i, "Share Fyuse to.."));
    }

    private boolean isFyuseFile(String filePath) {
        File file = new File(filePath);
        if (!FyuseFile.isSupport3DPanoramaSDK()) {
            return false;
        }
        if (FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_3)) {
            return true;
        }
        return FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_1);
    }

    private String getUserAgreementString(Context context) {
        return String.format("<a href = \"https://fyu.se/terms/hw\" >%s</a>", new Object[]{context.getResources().getString(R.string.m_USER_AGREEMENT)});
    }

    private View createPermissionContentView(Context ctx) {
        View contentView = LayoutInflater.from(ctx).inflate(R.layout.share_permisson_dlg, null);
        CheckBox checkBoxBtn = (CheckBox) contentView.findViewById(R.id.permission_remind);
        checkBoxBtn.setVisibility(0);
        checkBoxBtn.setChecked(true);
        TextView textContent = (TextView) contentView.findViewById(R.id.tv_dialog_content);
        textContent.setText(Html.fromHtml(String.format(ctx.getResources().getString(R.string.share_permission_content), new Object[]{getUserAgreementString(ctx)})));
        textContent.setMovementMethod(LinkMovementMethod.getInstance());
        textContent.setLinkTextColor(ctx.getResources().getColor(R.color.actionbar_hightlight_text_color_normal));
        textContent.postInvalidate();
        return contentView;
    }

    private void closePermissonDialog(Dialog dialog, boolean bSendRemind) {
        this.mPermissionDialog = null;
        if (dialog != null) {
            dialog.dismiss();
        }
        if (bSendRemind) {
            Intent intent = new Intent();
            intent.setClass(this.mActivity, UploadActivity.class);
            this.mActivity.startActivity(intent);
            CheckBox sendRemind = (CheckBox) dialog.findViewById(R.id.permission_remind);
            Editor editor = this.mPreference.edit();
            editor.putBoolean("is_never_remind", sendRemind.isChecked());
            editor.commit();
            sharFyuseFile();
        }
    }

    private void showPermissonDialog() {
        if (this.mPermissionDialog != null) {
            this.mPermissionDialog.show();
        } else if (this.mActivity != null) {
            Builder builder = new Builder(this.mActivity);
            builder.setTitle(R.string.freeshare_helper_title);
            builder.setNegativeButton(R.string.video_btn_cancel, this.mCancelClickListener);
            builder.setPositiveButton(R.string.ok, this.mAllowClickListener);
            this.mPermissionDialog = builder.create();
            this.mPermissionDialog.setView(createPermissionContentView(this.mActivity));
            this.mPermissionDialog.show();
        }
    }
}
