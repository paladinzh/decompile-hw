package com.android.contacts.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.widget.HapViewPager;
import com.android.contacts.speeddial.SpeedDialerActivity;
import com.google.android.gms.R;

public class ShowErrorDiallogUtils {
    private Fragment mFragment;

    public static class ErrorDialogFragment extends DialogFragment {
        private int mMessageResId;
        private int mTitleResId;

        public static ErrorDialogFragment newInstance(int messageResId) {
            return newInstance(0, messageResId);
        }

        public static ErrorDialogFragment newInstance(int titleResId, int messageResId) {
            ErrorDialogFragment fragment = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putInt("argTitleResId", titleResId);
            args.putInt("argMessageResId", messageResId);
            fragment.setArguments(args);
            return fragment;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mTitleResId = getArguments().getInt("argTitleResId");
            this.mMessageResId = getArguments().getInt("argMessageResId");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            if (this.mTitleResId != 0) {
                builder.setTitle(this.mTitleResId);
            }
            if (isAdded() && getActivity() != null) {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                TextView content = (TextView) view.findViewById(R.id.alert_dialog_content);
                if (this.mMessageResId != 0) {
                    content.setText(this.mMessageResId);
                    builder.setView(view);
                }
            } else if (this.mMessageResId != 0) {
                builder.setMessage(this.mMessageResId);
            }
            builder.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ErrorDialogFragment.this.dismiss();
                    if (ErrorDialogFragment.this.mMessageResId == R.string.dialog_voicemail_not_ready_message) {
                        ErrorDialogFragment.this.getActivity().startActivity(DialtactsActivity.getVoiceMailSettingsIntent());
                    }
                }
            });
            AlertDialog lDialog = builder.create();
            if (getActivity() instanceof PeopleActivity) {
                PeopleActivity peopleActivity = (PeopleActivity) getActivity();
                peopleActivity.mGlobalDialogReference = lDialog;
                HapViewPager viewPager = (HapViewPager) peopleActivity.getViewPager();
                if (viewPager != null) {
                    viewPager.disableViewPagerSlide(true);
                }
            } else if (getActivity() instanceof SpeedDialerActivity) {
                ((SpeedDialerActivity) getActivity()).mGlobalDialogReference = lDialog;
            }
            return lDialog;
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (getActivity() == null) {
                return;
            }
            if (getActivity() instanceof PeopleActivity) {
                PeopleActivity peopleActivity = (PeopleActivity) getActivity();
                peopleActivity.mGlobalDialogReference = null;
                HapViewPager viewPager = (HapViewPager) peopleActivity.getViewPager();
                if (viewPager != null) {
                    viewPager.disableViewPagerSlide(false);
                }
            } else if (getActivity() instanceof SpeedDialerActivity) {
                ((SpeedDialerActivity) getActivity()).mGlobalDialogReference = null;
            }
        }
    }

    public ShowErrorDiallogUtils(Fragment fragment) {
        this.mFragment = fragment;
    }

    public void showSIMNotAvailableForVoicemailDialog() {
        if (this.mFragment != null && this.mFragment.getFragmentManager() != null) {
            if (HwLog.HWDBG) {
                HwLog.d("ShowErrorDiallogUtils", "showSIMNotAvailableForVoicemailDialog");
            }
            ErrorDialogFragment.newInstance(R.string.voice_mail_text, R.string.msg_voicemail_sim_not_ready_Toast).show(this.mFragment.getFragmentManager(), "voicemail_sim_not_ready");
        }
    }

    public void showNotReadyForSpeedDialDialog() {
        if (this.mFragment != null && this.mFragment.getFragmentManager() != null) {
            if (HwLog.HWDBG) {
                HwLog.d("ShowErrorDiallogUtils", "showNotReadyForSpeedDialDialog");
            }
            ErrorDialogFragment.newInstance(R.string.title_speed_dial_settings, R.string.msg_speed_dial_airplane_mode_on).show(this.mFragment.getFragmentManager(), "speed_dial_not_ready");
        }
    }

    public void showVoiceMailNotReadyDialog() {
        if (this.mFragment != null && this.mFragment.getFragmentManager() != null) {
            if (HwLog.HWDBG) {
                HwLog.d("ShowErrorDiallogUtils", "showVoiceMailNotReadyDialog");
            }
            try {
                ErrorDialogFragment.newInstance(R.string.dialog_voicemail_not_ready_title, R.string.dialog_voicemail_not_ready_message).show(this.mFragment.getFragmentManager(), "voicemail_not_ready");
            } catch (IllegalStateException e) {
                HwLog.e("ShowErrorDiallogUtils", e.toString(), e);
            }
        }
    }

    public void showCannotMakeCallDialog() {
        if (this.mFragment != null && this.mFragment.getFragmentManager() != null) {
            if (HwLog.HWDBG) {
                HwLog.d("ShowErrorDiallogUtils", "showCannotMakeCallDialog");
            }
            ErrorDialogFragment.newInstance(R.string.dialog_phone_call_prohibited_message).show(this.mFragment.getFragmentManager(), "phone_prohibited_dialog");
        }
    }

    public void showAirplaneModeOnForVoicemailDialog() {
        if (this.mFragment != null && this.mFragment.getFragmentManager() != null) {
            if (HwLog.HWDBG) {
                HwLog.d("ShowErrorDiallogUtils", "showAirplaneModeOnForVoicemailDialog");
            }
            ErrorDialogFragment.newInstance(R.string.voice_mail_text, R.string.msg_voicemail_airplane_mode_on).show(this.mFragment.getFragmentManager(), "voicemail_airplane_mode_on");
        }
    }
}
