package com.android.contacts.hap.camcard.bcr;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import com.android.contacts.QuickPressPickNumberActivity;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.CCUtils;
import com.android.contacts.list.DefaultContactBrowseListFragment;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import java.io.File;

public class CCardScanHandler {
    private static final String TAG = CCardScanHandler.class.getSimpleName();
    private RecognizeRequest request = new RecognizeRequest();

    public static class RecognizeRequest {
        private String path;
        private Uri uri;

        private void addProcessIntentExtra(Intent intent) {
            intent.putExtra("output_path", this.path);
            intent.setData(this.uri);
        }
    }

    public void recognizeCapture(Activity activity, Fragment fragment) {
        boolean needStart = false;
        try {
            if (fragment instanceof DefaultContactBrowseListFragment) {
                needStart = ((DefaultContactBrowseListFragment) fragment).isCancelFromCcard;
            }
            if (!needStart) {
                fragment.startActivityForResult(createCaptureIntent(activity), VTMCDataCache.MAX_EXPIREDTIME);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.quickcontact_missing_app_Toast, 1).show();
        }
    }

    public boolean handlePhotoActivityResult(int requestCode, int resultCode, Intent data, Activity activity, Fragment fragment) {
        if (activity == null || fragment == null) {
            return false;
        }
        switch (requestCode) {
            case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
                if (resultCode == -1) {
                    if (!checkCamCardApkStates(activity) && (data == null || !"view".equals(data.getStringExtra("type")))) {
                        Toast.makeText(activity, R.string.camcard_failed_dialog_title, 1).show();
                        break;
                    }
                    handleCaptureResult(activity, fragment, data);
                    break;
                }
                HwLog.d(TAG, "REQUEST_CAPTURE cancel");
                break;
                break;
            case 301:
                if (fragment instanceof DefaultContactBrowseListFragment) {
                    boolean z;
                    DefaultContactBrowseListFragment defaultContactBrowseListFragment = (DefaultContactBrowseListFragment) fragment;
                    if (resultCode == 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    defaultContactBrowseListFragment.isCancelFromCcard = z;
                }
                if (resultCode != -1) {
                    if (resultCode == 0) {
                        recognizeCapture(activity, fragment);
                        break;
                    }
                } else if (!checkCamCardApkStates(activity)) {
                    Toast.makeText(activity, R.string.camcard_failed_dialog_title, 1).show();
                    break;
                } else {
                    fragment.startActivityForResult(createProcessIntentWithGellery(activity, data), 303);
                    break;
                }
                break;
            case 302:
            case 303:
                if (resultCode == 1) {
                    recognizeCapture(activity, fragment);
                    break;
                }
                break;
        }
        return false;
    }

    private void handleCaptureResult(Activity activity, Fragment fragment, Intent data) {
        if (data != null) {
            String type = data.getStringExtra("type");
            if ("single".equals(type)) {
                Intent intent = createProcessIntent(activity);
                if (!(intent == null || intent.resolveActivity(fragment.getActivity().getPackageManager()) == null)) {
                    fragment.startActivityForResult(intent, 302);
                }
            } else if ("multiple".equals(type)) {
                if (activity instanceof QuickPressPickNumberActivity) {
                    CCUtils.startCCardActivity(activity);
                    activity.finish();
                }
                activity.startService(createMultiRecognizeIntent(activity));
            } else if ("view".equals(type)) {
                fragment.startActivityForResult(createPhotoPickIntent(activity), 301);
            }
        }
    }

    private Intent createCaptureIntent(Context context) {
        Intent intent = new Intent("com.huawei.camera.cardreader.STILL_IMAGE");
        File rootFile = CCardPhotoUtils.getRootFilePath(context);
        if (rootFile != null) {
            String fileName = CCardPhotoUtils.generateTempPhotoFileDirectory();
            this.request.uri = CCardPhotoUtils.generateTmpPhotoFile(context, rootFile, fileName);
            this.request.path = rootFile.getAbsolutePath() + File.separator + fileName;
        }
        ContactPhotoUtils.addPhotoPickerExtras(intent, this.request.uri);
        intent.putExtra("output_path", this.request.path);
        return intent;
    }

    private Intent createProcessIntent(Activity activity) {
        Intent intent = createViewImageActivityIntent(activity);
        File[] pics = new File(this.request.path).listFiles();
        HwLog.d(TAG, "createProcessIntent");
        if (pics == null || pics[0] == null || this.request.uri == null) {
            HwLog.w(TAG, "camera error,no file");
        } else {
            this.request.path = pics[0].getAbsolutePath();
            this.request.uri = Uri.withAppendedPath(this.request.uri, pics[0].getName());
            this.request.addProcessIntentExtra(intent);
        }
        if (activity instanceof QuickPressPickNumberActivity) {
            intent.putExtra("from_to_quickaction", true);
        }
        return intent;
    }

    public static boolean isRequstPreciseResult(Context context) {
        if (context == null) {
            return false;
        }
        return SharePreferenceUtil.getDefaultSp_de(context).getBoolean("key_prefs_ccnotify", false);
    }

    private Intent createViewImageActivityIntent(Activity activity) {
        Intent intent = new Intent();
        intent.setPackage("com.huawei.contactscamcard");
        intent.setClassName("com.huawei.contactscamcard", "com.huawei.contactscamcard.bcr.ViewImageActivity");
        intent.putExtra("key_prefs_result_ccnotify", isRequstPreciseResult(activity));
        return intent;
    }

    private Intent createProcessIntentWithGellery(Activity activity, Intent data) {
        Intent intent = createViewImageActivityIntent(activity);
        if (activity instanceof QuickPressPickNumberActivity) {
            intent.putExtra("from_to_quickaction", true);
        }
        if (!(data == null || data.getData() == null)) {
            Uri uri = data.getData();
            File rootFile = CCardPhotoUtils.getRootFilePath(activity);
            if (rootFile == null) {
                HwLog.w(TAG, "rootFile null");
                return intent;
            }
            String fileName = CCardPhotoUtils.generateTempPhotoFileName();
            this.request.uri = CCardPhotoUtils.generateTmpPhotoFile(activity, rootFile, fileName);
            this.request.path = rootFile.getAbsolutePath() + File.separator + fileName;
            try {
                ContactPhotoUtils.savePhotoFromUriToUri(activity, uri, this.request.uri, false);
            } catch (SecurityException e) {
                HwLog.e(TAG, "Did not have read-access to uri.");
            }
            this.request.addProcessIntentExtra(intent);
        }
        return intent;
    }

    private Intent createPhotoPickIntent(Context context) {
        Intent intent = new Intent("android.intent.action.PICK", null);
        intent.setType("image/*");
        File rootFile = CCardPhotoUtils.getRootFilePath(context);
        if (rootFile == null) {
            return intent;
        }
        String fileName = CCardPhotoUtils.generateTempPhotoFileName();
        this.request.uri = CCardPhotoUtils.generateTmpPhotoFile(context, rootFile, fileName);
        this.request.path = rootFile.getAbsolutePath() + File.separator + fileName;
        ContactPhotoUtils.addPhotoPickerExtras(intent, this.request.uri);
        return intent;
    }

    public boolean checkCamCardApkStates(Context context) {
        EmuiFeatureManager.isShowCamCard(context);
        return EmuiFeatureManager.isCamcardEnabled();
    }

    public Intent createMultiRecognizeIntent(Context context) {
        Intent intent = CCSaveService.createMultiRecognizeIntent(context);
        intent.putExtra("output_path", this.request.path);
        intent.setAction("saveCard");
        intent.putExtra("key_prefs_result_ccnotify", isRequstPreciseResult(context));
        return intent;
    }

    public void onSaveInstance(Bundle bundle) {
        bundle.putParcelable("key_uri", this.request.uri);
        bundle.putString("key_path", this.request.path);
    }

    public static CCardScanHandler onRestoreInstance(Bundle bundle) {
        CCardScanHandler handler = new CCardScanHandler();
        handler.request.path = bundle.getString("key_path");
        handler.request.uri = (Uri) bundle.getParcelable("key_uri");
        return handler;
    }
}
