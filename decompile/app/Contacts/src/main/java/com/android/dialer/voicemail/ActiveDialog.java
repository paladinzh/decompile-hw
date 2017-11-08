package com.android.dialer.voicemail;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.dialer.voicemail.ActiveView.ButtonListener;
import com.google.android.gms.R;

public class ActiveDialog extends DialogFragment {
    private static final String TAG = ActiveDialog.class.getSimpleName();
    private ButtonListener mButtonListener = new ButtonListener() {
        public void onLaterButtonClick() {
            Bundle args = ActiveDialog.this.getArguments();
            if (args != null) {
                ActiveDialog.this.mPref.edit().putString("key_active_id", args.getString("key_active_id")).apply();
                HwLog.d(ActiveDialog.TAG, "ActiveDialog click not now button, id : " + args.getString("key_active_id"));
            }
            ActiveDialog.this.dismiss();
        }

        public void onActiveButtonClick() {
            Fragment targetFragment = ActiveDialog.this.getTargetFragment();
            if (targetFragment == null || !(targetFragment instanceof CallLogFragment)) {
                HwLog.e(ActiveDialog.TAG, "targetFragment : " + targetFragment);
            } else {
                ((CallLogFragment) targetFragment).showNotifacationDialog();
            }
            ActiveDialog.this.dismiss();
        }
    };
    private SharedPreferences mPref;

    public static ActiveDialog show(String todoActivedId, FragmentManager fm, Fragment targetFragment) {
        if (fm == null) {
            return null;
        }
        ActiveDialog dialog = new ActiveDialog();
        Bundle bundle = new Bundle();
        bundle.putString("key_active_id", todoActivedId);
        dialog.setArguments(bundle);
        dialog.setTargetFragment(targetFragment, 0);
        dialog.show(fm, TAG);
        return dialog;
    }

    public static ActiveDialog get(FragmentManager fm) {
        return fm == null ? null : (ActiveDialog) fm.findFragmentByTag(TAG);
    }

    public static boolean isNotNow(SharedPreferences sp, String todoActiveId) {
        String id = null;
        if (sp != null) {
            id = sp.getString("key_active_id", null);
        }
        return !TextUtils.isEmpty(id) ? id.equals(todoActiveId) : false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPref = SharePreferenceUtil.getDefaultSp_de(getActivity());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        ActiveView customView = (ActiveView) LayoutInflater.from(getActivity()).inflate(R.layout.active_dialog, null);
        customView.setButtonListener(this.mButtonListener);
        builder.setView(customView);
        return builder.create();
    }
}
