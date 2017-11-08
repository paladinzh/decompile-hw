package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsTransaction;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WarningDialog extends DialogFragment {
    private static final String TAG = WarningDialog.class.getSimpleName();
    private AlertDialog dialog = null;
    private boolean mDoNotHitStatus = false;

    private static WarningDialog showDialog(String warningText, FragmentManager fm) {
        WarningDialog dialog = new WarningDialog();
        Bundle args = new Bundle();
        args.putString("text", warningText);
        dialog.setArguments(args);
        dialog.show(fm, TAG);
        return dialog;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String warningText = getArguments().getString("text");
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.rcs_gallery_file_warning_dialog, null);
        TextView tx = (TextView) v.findViewById(R.id.gallery_file_waring_text);
        if (tx != null) {
            tx.setText(warningText);
        }
        this.dialog = new Builder(getActivity()).setView(v).setPositiveButton(17039379, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WarningDialog.saveRcsGalleryImageStatus(WarningDialog.this.getActivity(), !WarningDialog.this.mDoNotHitStatus);
                dialog.dismiss();
            }
        }).create();
        this.dialog.setCancelable(false);
        this.dialog.setCanceledOnTouchOutside(false);
        final CheckBox checkbox = (CheckBox) v.findViewById(R.id.gallery_file_not_remind_again);
        if (checkbox != null) {
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    WarningDialog.this.mDoNotHitStatus = checkbox.isChecked();
                }
            });
        }
        return this.dialog;
    }

    public boolean isShowing() {
        return this.dialog != null ? this.dialog.isShowing() : false;
    }

    private static void saveRcsGalleryImageStatus(Context context, boolean notAskMeAgain) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        MLog.i(TAG, "saveRcsCropImageStatus pref_key_crop_not_ask_me_again =  " + notAskMeAgain);
        sp.edit().putBoolean("pref_key_crop_not_ask_me_again", notAskMeAgain).apply();
    }

    private static String getWarninText(Context context, boolean[] preference) {
        if (preference[1] && preference[2]) {
            return String.format(context.getResources().getString(R.string.rcs_video_exceed_resolution_and_warn_size), new Object[]{"480p", Formatter.formatFileSize(context, ((long) RcsTransaction.getWarFileSizePermitedValue()) * 1024)});
        } else if (preference[2]) {
            return String.format(context.getResources().getString(R.string.rcs_video_exceed_resolution), new Object[]{"480p"});
        } else {
            MLog.d(TAG, "the video is exceed the warning size,but have not exceed resolution");
            return null;
        }
    }

    public static WarningDialog show(FragmentManager fm, Context context, String pathString) {
        boolean z;
        Uri uri = Uri.fromFile(new File(pathString));
        List<Uri> uriList = new ArrayList();
        uriList.add(uri);
        boolean[] preference = RcsTransaction.checkMediaResolution(context, uriList, new ArrayList(), new boolean[8]);
        if (preference[5]) {
            z = true;
        } else {
            z = preference[3];
        }
        if (z) {
            preference = getRcsGalleryImageStatus(context, preference);
            preference[1] = RcsTransaction.checkMediaFileSize(context, uriList);
            if (!preference[0] || !preference[3] || (!preference[1] && !preference[2])) {
                return null;
            }
            String warningText = getWarninText(context, preference);
            if (warningText != null) {
                return showDialog(warningText, fm);
            }
            return null;
        }
        MLog.i(TAG, "initWaringDialog ,but don't need compress ");
        return null;
    }

    private static boolean[] getRcsGalleryImageStatus(Context context, boolean[] preference) {
        preference[0] = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_crop_not_ask_me_again", true);
        MLog.i(TAG, "getRcsCropImageStatus pref_key_crop_not_ask_me_again =  " + preference[0]);
        return preference;
    }
}
