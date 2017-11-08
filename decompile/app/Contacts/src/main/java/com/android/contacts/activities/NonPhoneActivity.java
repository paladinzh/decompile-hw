package com.android.contacts.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.ContactsActivity;
import com.google.android.gms.R;

public class NonPhoneActivity extends ContactsActivity {

    public static final class NonPhoneDialogFragment extends DialogFragment implements OnClickListener {
        private int mThemeID;

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.mThemeID = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            if (this.mThemeID == 0) {
                this.mThemeID = 16973939;
            }
            AlertDialog alertDialog = new Builder(getActivity(), this.mThemeID).create();
            alertDialog.setTitle(R.string.non_phone_caption);
            if (!isAdded() || getActivity() == null) {
                alertDialog.setMessage(getArgumentPhoneNumber());
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getArgumentPhoneNumber());
                alertDialog.setView(view);
            }
            alertDialog.setButton(-1, getActivity().getString(R.string.non_phone_add_to_contacts), this);
            alertDialog.setButton(-2, getActivity().getString(R.string.non_phone_close), this);
            return alertDialog;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
                intent.setType("vnd.android.cursor.item/contact");
                intent.putExtra("phone", getArgumentPhoneNumber());
                startActivity(intent);
            }
            dismiss();
        }

        private String getArgumentPhoneNumber() {
            return getArguments().getString("PHONE_NUMBER");
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        setTheme(R.style.NonPhoneActivityTheme);
        String phoneNumber = getPhoneNumber();
        if (TextUtils.isEmpty(phoneNumber)) {
            finish();
            return;
        }
        NonPhoneDialogFragment fragment = new NonPhoneDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("PHONE_NUMBER", phoneNumber);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(fragment, "Fragment").commitAllowingStateLoss();
    }

    private String getPhoneNumber() {
        if (getIntent() == null) {
            return null;
        }
        Uri data = getIntent().getData();
        if (data == null) {
            return null;
        }
        if ("tel".equals(data.getScheme())) {
            return getIntent().getData().getSchemeSpecificPart();
        }
        return null;
    }
}
