package com.android.contacts.hap.camcard;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;

public class PreferenceDialog extends DialogFragment {
    private static final String TAG = PreferenceDialog.class.getSimpleName();

    private static class AboutSpan extends ClickableSpan {
        private Context context;

        private AboutSpan(Context context) {
            this.context = context;
        }

        public void onClick(View view) {
            Activity activity = this.context;
            activity.startActivity(new Intent(activity, AboutActivity.class));
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            int color = ImmersionUtils.getControlColor(this.context.getResources());
            if (color == 0) {
                color = this.context.getResources().getColor(R.color.download_selector_color_state_default);
            }
            ds.setColor(color);
            ds.setUnderlineText(false);
        }
    }

    public static void show(FragmentManager fm, Fragment tartgetFragment, int requestCode) {
        PreferenceDialog dialog = new PreferenceDialog();
        dialog.setTargetFragment(tartgetFragment, requestCode);
        dialog.show(fm, TAG);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.cc_notification_dialog, null);
        setSpanString((TextView) view.findViewById(R.id.notify_text), R.string.camcard_app_name, R.string.camcard_notify_content3);
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int i;
                boolean value = which == -1;
                PreferenceDialog.this.setPreference("key_prefs_ccnotify", value);
                PreferenceDialog.this.setResult(which);
                StatisticalHelper.sendReport(4020, value ? 1 : 0);
                if (value) {
                    i = 71;
                } else {
                    i = 72;
                }
                ExceptionCapture.reportScene(i);
            }
        };
        return new Builder(getActivity()).setTitle(getString(R.string.camcard_notify_title)).setView(view).setNegativeButton(getString(R.string.camcard_dialog_close), onClickListener).setPositiveButton(getString(R.string.camcard_dialog_open), onClickListener).create();
    }

    public void onCancel(DialogInterface dialog) {
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetFragment().getTargetRequestCode(), 1, new Intent());
        }
    }

    private void setResult(int which) {
        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            int resultCode = 0;
            if (which == -1) {
                resultCode = -1;
            }
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        }
    }

    private void setPreference(String key, boolean value) {
        Editor editor = SharePreferenceUtil.getDefaultSp_de(getActivity()).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void setSpanString(TextView textView, int resId, int resIdRef) {
        String href = getString(resId);
        String ref = getString(resIdRef, new Object[]{href});
        int start = ref.indexOf(href);
        int end = start + href.length();
        SpannableString sString = new SpannableString(ref);
        if (start >= 0 && end <= sString.length()) {
            sString.setSpan(new AboutSpan(getActivity()), start, end, 33);
        }
        textView.append(sString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
