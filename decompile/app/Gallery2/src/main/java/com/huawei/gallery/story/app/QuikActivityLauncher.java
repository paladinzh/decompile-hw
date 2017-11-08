package com.huawei.gallery.story.app;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.media.StoryAlbum;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.story.utils.LocationUtils;
import com.huawei.gallery.story.utils.StoryAlbumDateUtils;
import com.huawei.gallery.story.utils.StoryAlbumUtils;
import com.huawei.gallery.util.MyPrinter;
import java.io.File;
import java.util.ArrayList;

public class QuikActivityLauncher {
    private static final String[] LITTLE_PHOTO_TEMPLATE = new String[]{"picnic", "grammy", "bouncy", "swift", "blocky"};
    private static final MyPrinter LOG = new MyPrinter("QuikActivityLauncher");
    private static final String[] MORE_PHOTO_TEMPLATE = new String[]{"kinetic", "origami", "glide", "diamond"};
    private static final OnDismissListener sButtonDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            QuikActivityLauncher.sContext = null;
        }
    };
    private static String sClusterCode;
    private static Context sContext;
    private static final OnClickListener sPositiveButtonListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (QuikActivityLauncher.sContext != null) {
                QuikActivityLauncher.genSummaryAndGotoQuik(QuikActivityLauncher.sClusterCode, QuikActivityLauncher.sContext);
                QuikActivityLauncher.setQuikNetworkPermission(QuikActivityLauncher.sContext);
            }
        }
    };

    public static void launchQuikActivity(Context context, String clusterCode) {
        LOG.d("startQuik   clusterCode:" + clusterCode);
        switch (checkQuikPackageInfo((Activity) context)) {
            case -2:
                QuikDownloader.showQuikDownloadDialog((Activity) context, R.string.story_album_upgrade_quik_notes);
                return;
            case -1:
                QuikDownloader.showQuikDownloadDialog((Activity) context, R.string.story_album_download_quik_notes);
                return;
            default:
                if (getQuikNetworkPermission(context, clusterCode)) {
                    genSummaryAndGotoQuik(clusterCode, context);
                    return;
                }
                return;
        }
    }

    private static void genSummaryAndGotoQuik(String clusterCode, Context context) {
        StoryAlbum storyAlbum = StoryAlbumUtils.queryStoryAlbumInfo(clusterCode, context.getContentResolver());
        if (storyAlbum == null) {
            LOG.w("album info not found by cluster code " + clusterCode);
            return;
        }
        ArrayList<Uri> uriList = LocationUtils.getStoryAlbumSummaryUri(storyAlbum.getStoryId(), 30, context.getContentResolver());
        LOG.d("Uri list for Quik size is " + uriList.size());
        if (uriList.size() != 0) {
            jumpToQuikActivity(storyAlbum, uriList, context);
        }
    }

    private static void jumpToQuikActivity(StoryAlbum album, ArrayList<Uri> uris, Context context) {
        Intent intent = new Intent("android.intent.action.SEND_MULTIPLE");
        if (!TextUtils.isEmpty(album.getProjectId())) {
            intent.putExtra("project_id", album.getProjectId());
        }
        intent.putExtra("project_title", album.getStoryName());
        intent.putExtra("project_duration", 60);
        intent.putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
        intent.setType("image/*, video/*");
        intent.putExtra("aspect_ratio", "square");
        String timeString = StoryAlbumDateUtils.getDateString(System.currentTimeMillis(), "yyyyMMdd_HHmmss");
        File dir = GalleryUtils.createEmptyDir(new File(PhotoShareUtils.INNER_CAMERA_PATH));
        String videoPath = dir == null ? Environment.getExternalStorageDirectory().getAbsolutePath() : dir.getPath();
        LOG.d("quik video path = " + videoPath);
        intent.putExtra("video_file_path", videoPath + File.separator + "Story_" + timeString + ".mp4");
        intent.putExtra("add_assets", false);
        intent.putExtra("enable_external_project", true);
        intent.putExtra("enable_upload", false);
        intent.putExtra("branding_off", true);
        intent.putExtra("style", getRandomTemplate(uris.size() >= 16));
        intent.putExtra("random_style", false);
        intent.setFlags(1);
        intent.setPackage("com.stupeflix.replay");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            GalleryUtils.startActivityForResultCatchSecurityEx((Activity) context, intent, AbsAlbumPage.LAUNCH_QUIK_ACTIVITY);
        }
    }

    private static String getRandomTemplate(boolean morePhots) {
        String[] templateArray;
        if (morePhots) {
            templateArray = MORE_PHOTO_TEMPLATE;
        } else {
            templateArray = LITTLE_PHOTO_TEMPLATE;
        }
        int randomValue = (int) (System.currentTimeMillis() % ((long) templateArray.length));
        if (randomValue >= templateArray.length || randomValue < 0) {
            return "grammy";
        }
        LOG.d("random template is " + templateArray[randomValue]);
        return templateArray[randomValue];
    }

    public static void saveResultData(Context context, String clusterCode, Intent intent) {
        String projectId = intent.getStringExtra("project_id");
        StoryAlbum.setStoryAlbumProjectId(clusterCode, projectId, context.getContentResolver());
        String videoFilePath = intent.getStringExtra("video_file_path");
        LOG.d("SaveResultData  projectId = " + projectId + " videoFilePath = " + videoFilePath + " projectUri = " + intent.getData());
    }

    public static void errorProcess(Context context, String clusterCode, Intent intent) {
        if (intent != null) {
            switch (intent.getIntExtra("error_code", -1)) {
                case AbsAlbumPage.LAUNCH_QUIK_ACTIVITY /*400*/:
                    LOG.e("empty clipdata result open quik fail");
                    break;
                case 403:
                    LOG.d("launch quik fail with no permisson, retry with empty project id");
                    StoryAlbum.setStoryAlbumProjectId(clusterCode, "", context.getContentResolver());
                    genSummaryAndGotoQuik(clusterCode, context);
                    break;
            }
        }
    }

    private static int checkQuikPackageInfo(Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo("com.stupeflix.replay", 1);
            LOG.d("version name = " + info.versionName + " version code = " + info.versionCode);
            return 0;
        } catch (NameNotFoundException e) {
            LOG.d("Get package info:com.stupeflix.replay failed!, reason: NameNotFoundException.");
            return -1;
        }
    }

    private static boolean getQuikNetworkPermission(Context context, String clusterCode) {
        if (!GalleryUtils.IS_CHINESE_VERSION) {
            return true;
        }
        boolean allowNetworkAccess = GallerySettings.getBoolean(context, GallerySettings.KEY_QUIK_NETWORK_ACCESS_ALLOW, false);
        if (!allowNetworkAccess) {
            sClusterCode = clusterCode;
            showNetworkPermissionDialog(context);
        }
        return allowNetworkAccess;
    }

    private static void showNetworkPermissionDialog(Context context) {
        if (sContext == null) {
            sContext = context;
            new Builder(context).setMessage(R.string.network_access_notice).setPositiveButton(R.string.allow, sPositiveButtonListener).setNegativeButton(R.string.cancel, null).setOnDismissListener(sButtonDismissListener).show();
        }
    }

    private static void setQuikNetworkPermission(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(GallerySettings.KEY_QUIK_NETWORK_ACCESS_ALLOW, true);
        editor.apply();
    }
}
