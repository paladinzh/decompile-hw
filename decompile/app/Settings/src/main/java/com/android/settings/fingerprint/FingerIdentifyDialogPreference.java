package com.android.settings.fingerprint;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.fingerprint.Fingerprint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.fingerprint.FingerprintDialogPreference.Entry;
import java.util.ArrayList;
import java.util.List;

public class FingerIdentifyDialogPreference extends FingerprintDialogPreference {
    public FingerIdentifyDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mValidStartPos = 0;
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        builder.setNegativeButton(17039360, listener);
        builder.setPositiveButton(null, null);
        builder.setTitle(2131628682);
        builder.setCancelable(false);
    }

    protected void onBindDialogView(View view) {
        this.mClickedDialogEntryIndex = getValueIndex();
        ((TextView) view.findViewById(2131887180)).setText(2131628685);
        this.mListView = (ListView) view.findViewById(16908298);
        this.mListView.setAdapter(new ArrayAdapter(getContext(), 2130969132, this.mEntries));
        this.mListView.setItemsCanFocus(false);
    }

    protected void onClick(DialogInterface dialog, int which) {
        if (this.mDialogListener != null) {
            this.mDialogListener.onDialogClicked(dialog, which, this);
        }
    }

    protected void onClick() {
        List<Fingerprint> list = this.mBiometricManager.getFingerprintList(this.mUserId);
        this.mFingerNum = list.size();
        if (this.mFingerNum > 0) {
            generateEntries(list);
            super.onClick();
            if (this.mDialogListener != null) {
                this.mDialogListener.onDialogCreated(this);
            }
        }
    }

    private void generateEntries(List<Fingerprint> list) {
        int size = this.mFingerNum;
        ArrayList<Entry> entries = new ArrayList(size);
        for (Fingerprint fp : list) {
            entries.add(new Entry(fp.getName().toString(), String.valueOf(fp.getFingerId())));
        }
        this.mEntries = (Entry[]) entries.toArray(new Entry[size]);
    }
}
