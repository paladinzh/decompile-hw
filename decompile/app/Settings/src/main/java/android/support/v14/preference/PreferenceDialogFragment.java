package android.support.v14.preference;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.DialogPreference.TargetFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public abstract class PreferenceDialogFragment extends DialogFragment implements OnClickListener {
    private BitmapDrawable mDialogIcon;
    @LayoutRes
    private int mDialogLayoutRes;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence mNegativeButtonText;
    private CharSequence mPositiveButtonText;
    private DialogPreference mPreference;
    private int mWhichButtonClicked;

    public abstract void onDialogClosed(boolean z);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment rawFragment = getTargetFragment();
        if (rawFragment instanceof TargetFragment) {
            TargetFragment fragment = (TargetFragment) rawFragment;
            String key = getArguments().getString("key");
            Bitmap bitmap;
            if (savedInstanceState == null) {
                this.mPreference = (DialogPreference) fragment.findPreference(key);
                if (this.mPreference != null) {
                    this.mDialogTitle = this.mPreference.getDialogTitle();
                    this.mPositiveButtonText = this.mPreference.getPositiveButtonText();
                    this.mNegativeButtonText = this.mPreference.getNegativeButtonText();
                    this.mDialogMessage = this.mPreference.getDialogMessage();
                    this.mDialogLayoutRes = this.mPreference.getDialogLayoutResource();
                    Drawable icon = this.mPreference.getDialogIcon();
                    if (icon == null || (icon instanceof BitmapDrawable)) {
                        this.mDialogIcon = (BitmapDrawable) icon;
                        return;
                    }
                    bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    icon.draw(canvas);
                    this.mDialogIcon = new BitmapDrawable(getResources(), bitmap);
                    return;
                }
                return;
            }
            this.mDialogTitle = savedInstanceState.getCharSequence("PreferenceDialogFragment.title");
            this.mPositiveButtonText = savedInstanceState.getCharSequence("PreferenceDialogFragment.positiveText");
            this.mNegativeButtonText = savedInstanceState.getCharSequence("PreferenceDialogFragment.negativeText");
            this.mDialogMessage = savedInstanceState.getCharSequence("PreferenceDialogFragment.message");
            this.mDialogLayoutRes = savedInstanceState.getInt("PreferenceDialogFragment.layout", 0);
            bitmap = (Bitmap) savedInstanceState.getParcelable("PreferenceDialogFragment.icon");
            if (bitmap != null) {
                this.mDialogIcon = new BitmapDrawable(getResources(), bitmap);
                return;
            }
            return;
        }
        throw new IllegalStateException("Target fragment must implement TargetFragment interface");
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("PreferenceDialogFragment.title", this.mDialogTitle);
        outState.putCharSequence("PreferenceDialogFragment.positiveText", this.mPositiveButtonText);
        outState.putCharSequence("PreferenceDialogFragment.negativeText", this.mNegativeButtonText);
        outState.putCharSequence("PreferenceDialogFragment.message", this.mDialogMessage);
        outState.putInt("PreferenceDialogFragment.layout", this.mDialogLayoutRes);
        if (this.mDialogIcon != null) {
            outState.putParcelable("PreferenceDialogFragment.icon", this.mDialogIcon.getBitmap());
        }
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        this.mWhichButtonClicked = -2;
        Builder builder = new Builder(context).setTitle(this.mDialogTitle).setIcon(this.mDialogIcon).setPositiveButton(this.mPositiveButtonText, this).setNegativeButton(this.mNegativeButtonText, this);
        View contentView = onCreateDialogView(context);
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(this.mDialogMessage);
        }
        onPrepareDialogBuilder(builder);
        Dialog dialog = builder.create();
        if (needInputMethod()) {
            requestInputMethod(dialog);
        }
        return dialog;
    }

    public DialogPreference getPreference() {
        if (this.mPreference == null) {
            this.mPreference = (DialogPreference) ((TargetFragment) getTargetFragment()).findPreference(getArguments().getString("key"));
        }
        return this.mPreference;
    }

    protected void onPrepareDialogBuilder(Builder builder) {
    }

    protected boolean needInputMethod() {
        return false;
    }

    private void requestInputMethod(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(5);
    }

    protected View onCreateDialogView(Context context) {
        int resId = this.mDialogLayoutRes;
        if (resId == 0) {
            return null;
        }
        return LayoutInflater.from(context).inflate(resId, null);
    }

    protected void onBindDialogView(View view) {
        View dialogMessageView = view.findViewById(16908299);
        if (dialogMessageView != null) {
            CharSequence message = this.mDialogMessage;
            int newVisibility = 8;
            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView instanceof TextView) {
                    ((TextView) dialogMessageView).setText(message);
                }
                newVisibility = 0;
            }
            if (dialogMessageView.getVisibility() != newVisibility) {
                dialogMessageView.setVisibility(newVisibility);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mWhichButtonClicked = which;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onDialogClosed(this.mWhichButtonClicked == -1);
    }
}
