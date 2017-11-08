package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.MimeTypeHelper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.utils.FileTypeHelper;
import com.huawei.systemmanager.util.HwLog;

public class TrashDetailDialogFragment extends DialogFragment {
    public static final String ARG_TRASH_PATH = "arg_trash_path";
    public static final String ARG_TRASH_TYPE = "arg_trash_type";
    private static final int OP_CANCEL = 0;
    private static final int OP_PREVIEW = 1;
    private static final String TAG = "TrashDetailDialogFragment";
    private int mEndOfSdCardPath = 0;

    public static class CancelListener implements OnClickListener {
        private int mFileType;

        public CancelListener(int fileType) {
            this.mFileType = fileType;
        }

        public void onClick(DialogInterface dialog, int which) {
            SpaceStatsUtils.reportDownloadDetailDialogClick(this.mFileType, 0);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog;
        Bundle bundle = getArguments();
        final String path = getValidatePath(bundle);
        int type = bundle.getInt(ARG_TRASH_TYPE);
        final Context ctx = getApplicationContext();
        final int fileType = FileTypeHelper.getFileType(path);
        final String mimeType = MimeTypeHelper.getInstance().getMimeType(path);
        long time = FileUtil.getlastModified(path);
        if (time > 0) {
            String dataTime = DateUtils.formatDateTime(GlobalContext.getContext(), time, Constant.SPACECLEAN_VIDEO_TIME_FORMATTER);
            dialog = new Builder(getActivity()).setTitle(R.string.space_common_dialog_title_details).setMessage(getString(R.string.space_clean_file_dialog_details_content, new Object[]{dataTime, formatPath(path)})).setNegativeButton(R.string.common_cancel, new CancelListener(fileType)).create();
        } else {
            dialog = new Builder(getActivity()).setTitle(R.string.space_common_dialog_title_details).setMessage(getString(R.string.space_large_file_dialog_details_content, new Object[]{formatPath(path)})).setNegativeButton(R.string.common_cancel, new CancelListener(fileType)).create();
        }
        if (TextUtils.isEmpty(mimeType)) {
            HwLog.e(TAG, "mimeType is empty.trash type :" + type);
        } else {
            dialog.setButton(-1, getPositiveButtonText(fileType), new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    Activity ac = TrashDetailDialogFragment.this.getActivity();
                    if (ac != null) {
                        SpaceStatsUtils.reportDownloadDetailDialogClick(fileType, 1);
                        Intent intent = Utility.openReceivedFile(ctx, path, mimeType);
                        if (intent == null) {
                            Toast.makeText(ac, R.string.large_file_dialog_Toast_text, 0).show();
                            return;
                        }
                        try {
                            ac.startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            HwLog.i(TrashDetailDialogFragment.TAG, "no activity for handling ACTION_VIEW intent:  " + ex);
                        }
                    }
                }
            });
        }
        dialog.show();
        return dialog;
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    private String getPositiveButtonText(int type) {
        Context context = getApplicationContext();
        HwLog.i(TAG, "type = " + type);
        switch (type) {
            case 1:
            case 2:
                return context.getResources().getString(R.string.large_file_dialog_btn_text_play);
            case 4:
                return context.getResources().getString(R.string.large_file_dialog_btn_text_install);
            default:
                return context.getResources().getString(R.string.large_file_dialog_btn_text_view);
        }
    }

    private String formatPath(String path) {
        StringBuilder builder = new StringBuilder(path);
        builder.replace(0, this.mEndOfSdCardPath, "");
        return builder.toString();
    }

    private String getValidatePath(Bundle bundle) {
        String path = bundle.getString(ARG_TRASH_PATH);
        if (path != null) {
            return path;
        }
        HwLog.e(TAG, "path is empty!");
        return "";
    }
}
