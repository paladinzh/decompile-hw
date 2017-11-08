package com.android.contacts.hap.list;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ShowSimDialog extends DialogFragment {
    private String accountType;
    private SimDialogClickListener mListener;

    public static class SimDialogClickListener implements Parcelable {
        public static final Creator<SimDialogClickListener> CREATOR = new Creator<SimDialogClickListener>() {
            public SimDialogClickListener createFromParcel(Parcel source) {
                return new SimDialogClickListener();
            }

            public SimDialogClickListener[] newArray(int size) {
                return null;
            }
        };

        public void dialogClicked() {
        }

        public void onNotificationCancel() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    public static void show(FragmentManager aFragementManager, SimDialogClickListener aSimDialogClickListener, String accountType) {
        ShowSimDialog dialog = new ShowSimDialog();
        dialog.mListener = aSimDialogClickListener;
        dialog.accountType = accountType;
        try {
            dialog.show(aFragementManager, dialog.toString());
        } catch (IllegalStateException e) {
            HwLog.e("AlertDialog", "IllegalStateException catched");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mListener = (SimDialogClickListener) savedInstanceState.getParcelable("listener");
            this.accountType = savedInstanceState.getString("account_type");
        }
        if (SimFactoryManager.getSimConfig(this.accountType) == null) {
            return null;
        }
        int resId;
        int quantity = SimFactoryManager.getSimConfig(this.accountType).isANREnabled() ? 2 : 1;
        if (SimFactoryManager.getSimConfig(this.accountType).isEmailEnabled()) {
            resId = R.plurals.contact_str_copysim_copynamenumberemail;
        } else {
            resId = R.plurals.str_copysim_copynamenumber;
        }
        Builder builder = new Builder(getActivity()).setTitle(R.string.contact_str_copysim_notification).setPositiveButton(R.string.button_continue_text, new OnClickListener() {
            public void onClick(DialogInterface aDialogInterface, int which) {
                if (-1 == which && ShowSimDialog.this.mListener != null) {
                    ShowSimDialog.this.mListener.dialogClicked();
                }
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ShowSimDialog.this.mListener != null) {
                    ShowSimDialog.this.mListener.onNotificationCancel();
                }
            }
        });
        if (!isAdded() || getActivity() == null) {
            builder.setMessage(getResources().getQuantityString(resId, quantity, new Object[]{Integer.valueOf(quantity), SimFactoryManager.getSimCardDisplayLabel(this.accountType)}));
        } else {
            View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            TextView content = (TextView) view.findViewById(R.id.alert_dialog_content);
            content.setMovementMethod(ScrollingMovementMethod.getInstance());
            content.setText(getResources().getQuantityString(resId, quantity, new Object[]{Integer.valueOf(quantity), SimFactoryManager.getSimCardDisplayLabel(this.accountType)}));
            builder.setView(view);
        }
        return builder.create();
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (this.mListener != null) {
            this.mListener.onNotificationCancel();
        }
    }

    public void onSaveInstanceState(Bundle aBundle) {
        aBundle.putParcelable("listener", this.mListener);
        aBundle.putString("account_type", this.accountType);
        super.onSaveInstanceState(aBundle);
    }
}
