package com.android.settings.fingerprint;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.fingerprint.Fingerprint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.fingerprint.FingerprintDialogPreference.Entry;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import java.util.ArrayList;
import java.util.List;

public class ShortcutPaymentPreference extends FingerprintDialogPreference {
    private OnEnrollClickListener mEnrollListener;
    private int mPaymentStatus;

    public interface OnEnrollClickListener extends OnClickListener {
        void setFingerNum(int i);
    }

    public ShortcutPaymentPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPaymentStatus = 0;
        this.mValidStartPos = 1;
    }

    public void setEnrollListener(OnEnrollClickListener listener) {
        this.mEnrollListener = listener;
    }

    public void setCurrentFinger(Fingerprint currentFinger) {
        this.mCurrentFinger = currentFinger;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.mPaymentStatus = paymentStatus;
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        if (this.mFingerNum <= 1) {
            if (this.mEnrollListener != null) {
                this.mEnrollListener.setFingerNum(this.mFingerNum);
            } else {
                Log.e("ShortcutPaymentPreference", "onPrepareDialogBuilder mEnrollListener is null!");
            }
            builder.setPositiveButton(2131628215, listener);
        } else {
            builder.setPositiveButton(null, null);
        }
        builder.setNegativeButton(17039360, listener);
    }

    protected void onBindDialogView(View view) {
        this.mClickedDialogEntryIndex = getValueIndex();
        ((TextView) view.findViewById(2131887180)).setText(2131628567);
        this.mListView = (ListView) view.findViewById(16908298);
        this.mListView.setAdapter(new ArrayAdapter(getContext(), 2130969133, this.mEntries));
        this.mListView.setItemsCanFocus(true);
        this.mListView.setChoiceMode(1);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Entry entry = (Entry) adapter.getItemAtPosition(position);
                ShortcutPaymentPreference.this.mClickedDialogEntryIndex = position;
                if (ShortcutPaymentPreference.this.callChangeListener(entry.value)) {
                    ShortcutPaymentPreference.this.setValue(entry.value);
                }
                Dialog dialog = ShortcutPaymentPreference.this.getDialog();
                if (dialog != null) {
                    if (ShortcutPaymentPreference.this.mDialogListener != null) {
                        ShortcutPaymentPreference.this.mDialogListener.onDialogClicked(dialog, 0, ShortcutPaymentPreference.this);
                    }
                    dialog.dismiss();
                }
            }
        });
        if (this.mClickedDialogEntryIndex >= 0) {
            this.mListView.setSelection(this.mClickedDialogEntryIndex);
            this.mListView.setItemChecked(this.mClickedDialogEntryIndex, true);
        }
    }

    protected void onClick(DialogInterface dialog, int which) {
        if (this.mEnrollListener == null) {
            Log.e("ShortcutPaymentPreference", "onClick dialog mEnrollListener is null!");
            return;
        }
        if (which == -1) {
            if (this.mFingerNum <= 0) {
                this.mEnrollListener.setFingerNum(0);
            } else {
                this.mEnrollListener.setFingerNum(this.mFingerNum);
            }
            this.mEnrollListener.onClick(null, 0);
        }
        if (this.mDialogListener != null) {
            this.mDialogListener.onDialogClicked(dialog, which, this);
        }
    }

    protected void onClick() {
        if (this.mEnrollListener == null) {
            Log.e("ShortcutPaymentPreference", "onClick mEnrollListener is null");
            return;
        }
        List<Fingerprint> list = this.mBiometricManager.getFingerprintList();
        this.mFingerNum = list.size();
        if (this.mFingerNum <= 0) {
            this.mEnrollListener.setFingerNum(this.mFingerNum);
            this.mEnrollListener.onClick(null, 0);
        } else if (FingerprintUtils.getPaymentStatus(this.mContext) == 1 || this.mFingerNum != 1) {
            generateEntries(list);
            setValue(this.mCurrentFinger == null ? "shortcut_payment_none" : String.valueOf(this.mCurrentFinger.getFingerId()));
            super.onClick();
            if (this.mDialogListener != null) {
                this.mDialogListener.onDialogCreated(this);
            }
        } else {
            this.mEnrollListener.setFingerNum(this.mFingerNum);
            this.mEnrollListener.onClick(null, 0);
        }
    }

    private void generateEntries(List<Fingerprint> list) {
        int size = this.mFingerNum + 1;
        ArrayList<Entry> entries = new ArrayList(size);
        entries.add(new Entry(this.mContext.getString(2131628214), "shortcut_payment_none"));
        for (Fingerprint fp : list) {
            entries.add(new Entry(fp.getName().toString(), String.valueOf(fp.getFingerId())));
        }
        this.mEntries = (Entry[]) entries.toArray(new Entry[size]);
    }
}
