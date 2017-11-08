package com.android.contacts.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import com.android.contacts.editor.LabeledEditorView;
import com.google.android.gms.R;

public class DialogManager {
    private final Activity mActivity;
    private boolean mUseDialogId2 = false;

    public interface DialogShowingViewActivity {
        DialogManager getDialogManager();
    }

    public interface DialogShowingView {
        Dialog createDialog(Bundle bundle);
    }

    public static final boolean isManagedId(int id) {
        return id == R.id.dialog_manager_id_1 || id == R.id.dialog_manager_id_2;
    }

    public DialogManager(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        this.mActivity = activity;
    }

    public void showDialogInView(View view, Bundle bundle) {
        int viewId = view.getId();
        if (bundle.containsKey("view_id")) {
            throw new IllegalArgumentException("Bundle already contains a view_id");
        } else if (viewId == -1) {
            throw new IllegalArgumentException("View does not have a proper ViewId");
        } else {
            bundle.putInt("view_id", viewId);
            this.mActivity.showDialog(this.mUseDialogId2 ? R.id.dialog_manager_id_2 : R.id.dialog_manager_id_1, bundle);
        }
    }

    public Dialog onCreateDialog(final int id, Bundle bundle) {
        if (id == R.id.dialog_manager_id_1) {
            this.mUseDialogId2 = true;
        } else if (id != R.id.dialog_manager_id_2) {
            return null;
        } else {
            this.mUseDialogId2 = false;
        }
        if (bundle.containsKey("view_id")) {
            final View view = this.mActivity.findViewById(bundle.getInt("view_id"));
            if (view == null || !(view instanceof DialogShowingView)) {
                return null;
            }
            Dialog dialog = ((DialogShowingView) view).createDialog(bundle);
            if (dialog == null) {
                return dialog;
            }
            dialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    DialogManager.this.mActivity.removeDialog(id);
                    if (view instanceof LabeledEditorView) {
                        LabeledEditorView lView = view;
                        if (!DialogManager.this.mActivity.isChangingConfigurations()) {
                            lView.setIfDataPickerShouldBeDisplayed(false);
                        }
                    }
                }
            });
            return dialog;
        }
        throw new IllegalArgumentException("Bundle does not contain a ViewId");
    }
}
