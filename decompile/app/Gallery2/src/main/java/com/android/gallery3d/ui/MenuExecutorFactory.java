package com.android.gallery3d.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.util.GalleryUtils;

public class MenuExecutorFactory {
    private static final /* synthetic */ int[] -com-android-gallery3d-ui-MenuExecutorFactory$StyleSwitchesValues = null;

    public enum Style {
        NORMAL_STYLE,
        PASTE_STYLE,
        SHARE_TRANS_STYLE,
        WAIT_STYLE
    }

    private static /* synthetic */ int[] -getcom-android-gallery3d-ui-MenuExecutorFactory$StyleSwitchesValues() {
        if (-com-android-gallery3d-ui-MenuExecutorFactory$StyleSwitchesValues != null) {
            return -com-android-gallery3d-ui-MenuExecutorFactory$StyleSwitchesValues;
        }
        int[] iArr = new int[Style.values().length];
        try {
            iArr[Style.NORMAL_STYLE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Style.PASTE_STYLE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Style.SHARE_TRANS_STYLE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Style.WAIT_STYLE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-gallery3d-ui-MenuExecutorFactory$StyleSwitchesValues = iArr;
        return iArr;
    }

    private static ProgressDialog createNomalProgressDialog(Context context, int titleId, int progressMax) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setMax(progressMax);
        if (progressMax > 1) {
            dialog.setTitle(titleId);
            dialog.setProgressStyle(1);
        } else {
            dialog.setMessage(context.getString(titleId));
            dialog.setProgressStyle(0);
        }
        return dialog;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getPasteMessage(GalleryContext activity, Bundle data) {
        if (activity == null || data == null || data.getString("key-targetfilename") == null) {
            return null;
        }
        int messageStringId;
        if (data.getInt("key-pastestate", 0) == 1) {
            messageStringId = R.string.pasteing_message_copy;
        } else {
            messageStringId = R.string.pasteing_message_cut;
        }
        return activity.getString(messageStringId, mediaSetName);
    }

    private static ProgressDialog createPasteProgressDialog(final GalleryContext activity, int titleId, final MenuExecutor executor, Bundle data) {
        ProgressDialog dialog = new ProgressDialog(activity.getActivityContext());
        dialog.setTitle(titleId);
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        String messageString = getPasteMessage(activity, data);
        if (messageString != null) {
            dialog.setMessage(messageString);
        }
        dialog.setProgressStyle(1);
        dialog.setProgressNumberFormat(activity.getString(R.string.please_wait));
        dialog.setProgressPercentFormat(GalleryUtils.getPercentFormat(0));
        dialog.setButton(-1, activity.getString(R.string.cancel), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final DataManager manager = activity.getDataManager();
                final MenuExecutor menuExecutor = executor;
                new Thread(new Runnable() {
                    public void run() {
                        manager.onPasteCanceled(null, menuExecutor);
                    }
                }).start();
            }
        });
        return dialog;
    }

    private static ProgressDialog createShareTransProgressDialog(GalleryContext activity) {
        ProgressDialog dialog = new ProgressDialog(activity.getActivityContext());
        dialog.setTitle(activity.getString(R.string.translate_vi_ing_title));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgressPercentFormat(GalleryUtils.getPercentFormat(0));
        dialog.setProgressNumberFormat(null);
        dialog.setProgressStyle(1);
        return dialog;
    }

    public static ProgressDialog create(GalleryContext activity, int titleId, int progressMax, MenuExecutor executor, Style style, Bundle data) {
        switch (-getcom-android-gallery3d-ui-MenuExecutorFactory$StyleSwitchesValues()[style.ordinal()]) {
            case 2:
                return createPasteProgressDialog(activity, titleId, executor, data);
            case 3:
                return createShareTransProgressDialog(activity);
            case 4:
                return createNomalProgressDialog(activity.getActivityContext(), R.string.please_wait, 1);
            default:
                return createNomalProgressDialog(activity.getActivityContext(), titleId, progressMax);
        }
    }
}
